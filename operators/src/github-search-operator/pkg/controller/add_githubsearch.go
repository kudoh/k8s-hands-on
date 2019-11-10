package controller

import (
	"github-search-operator/pkg/controller/githubsearch"
)

func init() {
	// AddToManagerFuncs is a list of functions to create controllers and add them to a manager.
	AddToManagerFuncs = append(AddToManagerFuncs, githubsearch.Add)
}
