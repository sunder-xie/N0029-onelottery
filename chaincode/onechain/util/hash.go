package util

import (
	"crypto/sha1"
	"fmt"
)

func Hash(s string) string {
	h := sha1.New()
	h.Write([]byte(s))
	return fmt.Sprintf("%x", h.Sum(nil))
}
