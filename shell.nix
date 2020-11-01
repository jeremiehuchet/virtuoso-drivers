{
  systemPkgs ? import <nixpkgs> { },
  pkgs ? import (systemPkgs.fetchFromGitHub {
    owner = "nixos";
    repo = "nixpkgs";
    rev = "20.03";
    hash = "sha256:0182ys095dfx02vl2a20j1hz92dx3mfgz2a6fhn31bqlp1wa8hlq";
  }) { }
}:

let
  dependencies = import ./dependencies.nix { inherit pkgs; };
in pkgs.mkShell {
  name = "virtuoso-build-env";

  buildInputs = with pkgs; [ gitAndTools.gitFull gitAndTools.hub jq nix-prefetch-github maven xmlstarlet dependencies.dhall-json ];

  LC_ALL = "C";
  JDK6_HOME = dependencies.jdk6;
  JDK7_HOME = dependencies.jdk7;
  JDK8_HOME = dependencies.jdk8;
  JDK9_HOME = dependencies.jdk9;
}
