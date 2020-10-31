#!/usr/bin/env bash
set -e
wget --quiet https://github.com/jeremiehuchet/virtuoso-drivers/releases/download/build-dependencies/jdk-6u45-linux-x64.bin.gpg
gpg --quiet --batch --yes --decrypt --passphrase="$GPG_PASSPHRASE_JDK6" jdk-6u45-linux-x64.bin.gpg > jdk-6u45-linux-x64.bin
nix-store --add-fixed sha256 jdk-6u45-linux-x64.bin
rm jdk-6u45-linux-x64.bin
mkdir -p ~/.nixpkgs
echo '{ allowUnfree = true; }' >  ~/.nixpkgs/config.nix
