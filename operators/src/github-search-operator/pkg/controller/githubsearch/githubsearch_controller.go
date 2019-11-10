package githubsearch

import (
	"context"
	"fmt"
	"reflect"

	"k8s.io/apimachinery/pkg/api/resource"
	"k8s.io/apimachinery/pkg/util/intstr"
	"sigs.k8s.io/controller-runtime/pkg/controller/controllerutil"

	mamezouv1 "github-search-operator/pkg/apis/mamezou/v1"
	appv1 "k8s.io/api/apps/v1"
	corev1 "k8s.io/api/core/v1"
	"k8s.io/apimachinery/pkg/api/errors"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/runtime"
	"k8s.io/apimachinery/pkg/types"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/controller"
	"sigs.k8s.io/controller-runtime/pkg/handler"
	logf "sigs.k8s.io/controller-runtime/pkg/log"
	"sigs.k8s.io/controller-runtime/pkg/manager"
	"sigs.k8s.io/controller-runtime/pkg/reconcile"
	"sigs.k8s.io/controller-runtime/pkg/source"
)

var log = logf.Log.WithName("controller_githubsearch")

const (
	defaultAppVersion  = "v1.2"
	defaultClusterSize = 1
)

// Add creates a new GithubSearch Controller and adds it to the Manager. The Manager will set fields on the Controller
// and Start it when the Manager is Started.
func Add(mgr manager.Manager) error {
	return add(mgr, newReconciler(mgr))
}

// newReconciler returns a new reconcile.Reconciler
func newReconciler(mgr manager.Manager) reconcile.Reconciler {
	return &ReconcileGithubSearch{client: mgr.GetClient(), scheme: mgr.GetScheme()}
}

// add adds a new Controller to mgr with r as the reconcile.Reconciler
func add(mgr manager.Manager, r reconcile.Reconciler) error {
	// Create a new controller
	c, err := controller.New("githubsearch-controller", mgr, controller.Options{Reconciler: r})
	if err != nil {
		return err
	}

	// Watch for changes to primary resource GithubSearch
	err = c.Watch(&source.Kind{Type: &mamezouv1.GithubSearch{}}, &handler.EnqueueRequestForObject{})
	if err != nil {
		return err
	}

	return nil
}

// blank assignment to verify that ReconcileGithubSearch implements reconcile.Reconciler
var _ reconcile.Reconciler = &ReconcileGithubSearch{}

// ReconcileGithubSearch reconciles a GithubSearch object
type ReconcileGithubSearch struct {
	// This client, initialized using mgr.Client() above, is a split client
	// that reads objects from the cache and writes to the apiserver
	client client.Client
	scheme *runtime.Scheme
}

// Reconcile reads that state of the cluster for a GithubSearch object and makes changes based on the state read
// and what is in the GithubSearch.Spec
// Note:
// The Controller will requeue the Request to be processed again if the returned error is non-nil or
// Result.Requeue is true, otherwise upon completion it will remove the work from the queue.
func (r *ReconcileGithubSearch) Reconcile(request reconcile.Request) (reconcile.Result, error) {
	reqLogger := log.WithValues("Request.Namespace", request.Namespace, "Request.Name", request.Name)
	reqLogger.Info("Reconciling GithubSearch")

	// Fetch the GithubSearch instance
	instance := &mamezouv1.GithubSearch{}
	err := r.client.Get(context.TODO(), request.NamespacedName, instance)
	if err != nil {
		if errors.IsNotFound(err) {
			// Request object not found, could have been deleted after reconcile request.
			// Owned objects are automatically garbage collected. For additional cleanup logic use finalizers.
			// Return and don't requeue
			return reconcile.Result{}, nil
		}
		// Error reading the object - requeue the request.
		return reconcile.Result{}, err
	}

	// Synchronize Service Resource
	service := newService(instance)
	existingSvc := &corev1.Service{}
	svcKeys := keys(instance.Namespace, "Service")
	err = r.client.Get(context.TODO(), types.NamespacedName{Namespace: instance.Namespace, Name: instance.Name}, existingSvc)
	if err != nil {
		if errors.IsNotFound(err) {
			// Serviceが存在しない -> 新規生成
			reqLogger.Info("Creating new Service", svcKeys...)
			_ = controllerutil.SetControllerReference(instance, service, r.scheme)
			if err := r.client.Create(context.TODO(), service); err != nil {
				return reconcile.Result{}, err
			}
			reqLogger.Info("Created!!", svcKeys...)
		} else {
			return reconcile.Result{}, err
		}
	} else {
		service.Spec.ClusterIP = existingSvc.Spec.ClusterIP
		if !reflect.DeepEqual(service.Spec, existingSvc.Spec) {
			// 既存サービスがCustom Resourceに定義されたDesiredな状態でない -> 更新
			reqLogger.Info("Updating existing Service", svcKeys...)
			service.ObjectMeta = existingSvc.ObjectMeta
			service.Spec.ClusterIP = existingSvc.Spec.ClusterIP
			_ = controllerutil.SetControllerReference(instance, service, r.scheme)
			if err := r.client.Update(context.TODO(), service); err != nil {
				return reconcile.Result{}, err
			}
			reqLogger.Info("Updated!!", svcKeys...)
		}
	}

	// Synchronize Deployment Resource
	secret, err := getSecretName(r, instance.Namespace, &instance.Spec)
	if err != nil {
		return reconcile.Result{}, err
	}
	deployment := newDeployment(instance, secret)
	existingDeploy := &appv1.Deployment{}
	deployKeys := keys(instance.Namespace, "Deployment")
	err = r.client.Get(context.TODO(), types.NamespacedName{Namespace: instance.Namespace, Name: instance.Name}, existingDeploy)
	if err != nil {
		if errors.IsNotFound(err) {
			// Deploymentが存在しない -> 新規生成
			reqLogger.Info("Creating new Deployment", deployKeys...)
			_ = controllerutil.SetControllerReference(instance, deployment, r.scheme)
			if err := r.client.Create(context.TODO(), deployment); err != nil {
				return reconcile.Result{}, err
			}
			reqLogger.Info("Created!!", deployKeys...)
		} else {
			return reconcile.Result{}, err
		}
	} else if !reflect.DeepEqual(deployment.Spec, existingDeploy.Spec) {
		// 既存サービスがCustom Resourceに定義されたDesiredな状態でない -> 更新
		reqLogger.Info("Updating existing Deployment", deployKeys...)
		service.ObjectMeta = existingSvc.ObjectMeta
		_ = controllerutil.SetControllerReference(instance, deployment, r.scheme)
		if err := r.client.Update(context.TODO(), deployment); err != nil {
			return reconcile.Result{}, err
		}
		reqLogger.Info("Updated!!", deployKeys...)
	}

	_ = r.client.Status().Update(context.TODO(), instance)

	reqLogger.Info("Completed GithubOperator Reconciliation!!")
	return reconcile.Result{}, nil // Done!!
}

// Creates github Service resource manifest
func newService(cr *mamezouv1.GithubSearch) *corev1.Service {

	serviceType := corev1.ServiceTypeClusterIP
	if cr.Spec.ServiceType != "" {
		serviceType = corev1.ServiceType(cr.Spec.ServiceType)
	}

	return &corev1.Service{
		TypeMeta: metav1.TypeMeta{
			Kind:       "Service",
			APIVersion: "v1",
		},
		ObjectMeta: metav1.ObjectMeta{
			Name:      cr.Name,
			Labels:    map[string]string{"app": cr.Name},
			Namespace: cr.Namespace,
		},
		Spec: corev1.ServiceSpec{
			Type:            serviceType,
			Selector:        map[string]string{"app": cr.Name},
			SessionAffinity: corev1.ServiceAffinityNone,
			Ports: []corev1.ServicePort{
				{
					Name:       "http",
					Protocol:   "TCP",
					TargetPort: intstr.FromString("http"),
					Port:       80,
				},
			},
		},
	}
}

func getSecretName(r *ReconcileGithubSearch, namespace string, spec *mamezouv1.GithubSearchSpec) (secretName string, err error) {

	if spec.GithubSecret != "" {
		secretName = spec.GithubSecret
	} else {
		secretName = "github-secret"
	}

	secret := &corev1.Secret{}
	err = r.client.Get(context.TODO(), types.NamespacedName{Namespace: namespace, Name: secretName}, secret)
	if err != nil {
		return
	} else {
		secretName = secret.Name
		return
	}
}

// Creates github-service deployment manifest
func newDeployment(cr *mamezouv1.GithubSearch, secretName string) *appv1.Deployment {

	labels := map[string]string{
		"app": cr.Name,
	}

	probe := &corev1.Probe{
		Handler: corev1.Handler{
			HTTPGet: &corev1.HTTPGetAction{
				Path: "/actuator/health",
				Port: intstr.FromInt(8080),
			},
		},
		InitialDelaySeconds: 30,
		TimeoutSeconds:      5,
	}

	return &appv1.Deployment{
		TypeMeta: metav1.TypeMeta{
			Kind:       "Deployment",
			APIVersion: "apps/v1",
		},
		ObjectMeta: metav1.ObjectMeta{
			Name:      cr.Name,
			Labels:    labels,
			Namespace: cr.Namespace,
		},
		Spec: appv1.DeploymentSpec{
			Replicas: getReplicaSize(cr),
			Strategy: appv1.DeploymentStrategy{
				Type: appv1.DeploymentStrategyType("RollingUpdate"),
			},
			Selector: &metav1.LabelSelector{
				MatchLabels: labels,
			},
			Template: corev1.PodTemplateSpec{
				ObjectMeta: metav1.ObjectMeta{
					Labels: labels,
				},
				Spec: corev1.PodSpec{
					Containers: []corev1.Container{
						{
							Name:            "github-service",
							Image:           fmt.Sprintf("kudohn/github-service:%s", getAppVersion(cr)),
							ImagePullPolicy: "IfNotPresent",
							Ports: []corev1.ContainerPort{
								{
									Name:          "http",
									ContainerPort: 8080,
									Protocol:      "TCP",
								},
							},
							ReadinessProbe: probe,
							LivenessProbe:  probe,
							Lifecycle: &corev1.Lifecycle{
								PreStop: &corev1.Handler{
									Exec: &corev1.ExecAction{
										Command: []string{"sh", "-c", "sleep 5"},
									},
								},
							},
							Env: []corev1.EnvVar{
								{
									Name: "GITHUB_USER",
									ValueFrom: &corev1.EnvVarSource{
										SecretKeyRef: &corev1.SecretKeySelector{
											LocalObjectReference: corev1.LocalObjectReference{
												Name: secretName,
											},
											Key: "user",
										},
									},
								}, {
									Name: "GITHUB_PASSWORD",
									ValueFrom: &corev1.EnvVarSource{
										SecretKeyRef: &corev1.SecretKeySelector{
											LocalObjectReference: corev1.LocalObjectReference{
												Name: secretName,
											},
											Key: "password",
										},
									},
								},
							},
							Resources: corev1.ResourceRequirements{
								Requests: corev1.ResourceList{
									corev1.ResourceCPU:    resource.MustParse("200m"),
									corev1.ResourceMemory: resource.MustParse("1Gi"),
								},
								Limits: corev1.ResourceList{
									corev1.ResourceCPU:    resource.MustParse("200m"),
									corev1.ResourceMemory: resource.MustParse("1Gi"),
								},
							},
						},
					},
				},
			},
		},
	}
}

func getReplicaSize(cr *mamezouv1.GithubSearch) *int32 {
	if cr.Spec.ClusterSize > 0 {
		return &cr.Spec.ClusterSize
	}
	// default size
	r := int32(defaultClusterSize)
	return &r
}

func getAppVersion(cr *mamezouv1.GithubSearch) string {
	if cr.Spec.AppVersion != "" {
		return cr.Spec.AppVersion
	}
	// default version
	return defaultAppVersion
}

func keys(ns string, kind string) []interface{} {
	return []interface{}{"Namespace", ns, "Deployment", kind}
}
