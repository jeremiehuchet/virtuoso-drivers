{ pkgs }:

let

  mkDerivation = pkgs.stdenv.mkDerivation;
  fetchurl = pkgs.fetchurl;
  fetchFromGitHub = pkgs.fetchFromGitHub;

  pkgs_16_03 = import (fetchFromGitHub {
    owner = "nixos";
    repo = "nixpkgs";
    rev = "88c9f8b574ead01e32c5ee5228679723343cd52c";
    hash = "sha256:0w8dnyhnhld4gixyaq387pqzlp0r56g2hdfqjgb6fka4m9jrxlig";
  }) { };

  pkgs_18_03 = import (fetchFromGitHub {
    owner = "nixos";
    repo = "nixpkgs";
    rev = "3e1be2206b4c1eb3299fb633b8ce9f5ac1c32898";
    hash = "sha256:11d01fdb4d1avi7564h7w332h3jjjadsqwzfzpyh5z56s6rfksxc";
  }) { };

  pkgs_20_03 = import (fetchFromGitHub {
    owner = "nixos";
    repo = "nixpkgs";
    rev = "20.09";
    hash = "sha256:1wg61h4gndm3vcprdcg7rc4s1v3jkm5xd7lw8r2f67w502y94gcy";
  }) { };

in {

  bison_2_3 = mkDerivation {
    name = "bison-2.3";
    src = fetchurl {
      url = "mirror://gnu/bison/bison-2.3.tar.bz2";
      hash = "sha256:1yyzl3pcspsv6rbhh877shkwbd97vlcwy4a99vp2mrsb6ng7w3di";
    };
    buildInputs = [ pkgs.gnum4 ];
  };

  gperf_3_1 = mkDerivation {
    name = "gperf-3.1";
    src = fetchurl {
      url = "mirror://gnu/gperf/gperf-3.1.tar.gz";
      hash = "sha256:1qispg6i508rq8pkajh26cznwimbnj06wq9sd85vg95v8nwld1aq";
    };
  };

  openssl_1_0_2 = pkgs_18_03.openssl_1_0_2;

  jdk6 = pkgs_16_03.oraclejdk;

  jdk7 = pkgs_18_03.openjdk7;

  jdk8 = pkgs_18_03.openjdk8;

  jdk9 = pkgs_18_03.openjdk9;

  dhall-json = pkgs_20_03.dhall-json;
}
