package v1

import (
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
)

// GithubSearchSpec defines the desired state of GithubSearch
// +k8s:openapi-gen=true
type GithubSearchSpec struct {
	// +optional
	GithubSecret string `json:"githubSecret"`
	// +optional
	ClusterSize int32 `json:"clusterSize"`
	// +optional
	ServiceType string `json:"serviceType"`
	// +optional
	AppVersion string `json:"appVersion"`
}

// GithubSearchStatus defines the observed state of GithubSearch
// +k8s:openapi-gen=true
type GithubSearchStatus struct {
}

// +k8s:deepcopy-gen:interfaces=k8s.io/apimachinery/pkg/runtime.Object

// GithubSearch is the Schema for the githubsearches API
// +k8s:openapi-gen=true
// +kubebuilder:subresource:status
// +kubebuilder:resource:path=githubsearches,scope=Namespaced
type GithubSearch struct {
	metav1.TypeMeta   `json:",inline"`
	metav1.ObjectMeta `json:"metadata,omitempty"`

	Spec   GithubSearchSpec   `json:"spec,omitempty"`
	Status GithubSearchStatus `json:"status,omitempty"`
}

// +k8s:deepcopy-gen:interfaces=k8s.io/apimachinery/pkg/runtime.Object

// GithubSearchList contains a list of GithubSearch
type GithubSearchList struct {
	metav1.TypeMeta `json:",inline"`
	metav1.ListMeta `json:"metadata,omitempty"`
	Items           []GithubSearch `json:"items"`
}

func init() {
	SchemeBuilder.Register(&GithubSearch{}, &GithubSearchList{})
}
