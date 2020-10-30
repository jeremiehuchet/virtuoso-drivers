{ pkgs ? import <nixpkgs> { } }:

with pkgs;

let
  deps = import ./dependencies.nix { inherit pkgs; };
  github = pkgs.lib.importJSON ./virtuoso-github.json;
  jshell = "${pkgs.jdk11}/bin/jshell";
  printJdbcDriverBuildVersion = writeText "printJdbcDriverBuildVersion.jsh" ''
    var driver = new virtuoso.jdbc4.Driver();
    System.out.printf("%d.%d", driver.getMajorVersion(), driver.getMinorVersion());
    /exit
  '';
in stdenv.mkDerivation rec {
  name = "virtuoso-jdbc-sources-${github.rev}";

  src = fetchFromGitHub {
    inherit (github) owner repo rev sha256;
  };

  nativeBuildInputs =
    [ autoreconfHook gnum4 unixtools.netstat deps.bison_2_3 deps.gperf_3_1 ];

  buildInputs = [ flex deps.openssl_1_0_2 ];

  patchPhase = ''
    echo "deleting JAR files in order to make the build scripts rebuild them"
    rm -v libsrc/JDBCDriverType4/virtjdbc*.jar

    echo "patching makefiles to avoid building everything"
    # we just need to run the "C-style make" source file processing
    # in order to get the java source files to compile
    sed -i 's/^SUBDIRS = .*/SUBDIRS = libsrc/'          Makefile.am
    sed -i 's/^SUBDIRS = .*/SUBDIRS = JDBCDriverType4/' libsrc/Makefile.am
  '';

  configureFlags =
    [ "--with-jdk4=${deps.jdk6}" "--with-jdk4_1=${deps.jdk7}"  "--with-jdk4_2=${deps.jdk8}" ];

  postInstall = ''
    # remove anything but *.java files
    find -type f ! -name '*.java' -delete

    # keep jdbc driver generated source files
    cp -r libsrc/JDBCDriverType4/virtuoso/jdbc/outstd4   $out/4.0
    cp -r libsrc/JDBCDriverType4/virtuoso/jdbc/outstd4_1 $out/4.1
    cp -r libsrc/JDBCDriverType4/virtuoso/jdbc/outstd4_2 $out/4.2

    # all jars should have the same build version
    VERSION_40=$(${jshell} --class-path $out/lib/jdbc-4.0/virtjdbc4.jar   ${printJdbcDriverBuildVersion})
    VERSION_41=$(${jshell} --class-path $out/lib/jdbc-4.1/virtjdbc4_1.jar ${printJdbcDriverBuildVersion})
    VERSION_42=$(${jshell} --class-path $out/lib/jdbc-4.2/virtjdbc4_2.jar ${printJdbcDriverBuildVersion})
    [ "$VERSION_40" == "$VERSION_41" ] && [ "$VERSION_40" == "$VERSION_42" ] || exit 1
    echo "$VERSION_40" > $out/build-version

    find $out -type d -empty -delete
  '';
}
