# Firebase Studio (IDX) environment configuration for JVA-bookstore
{ pkgs, ... }: {
  # Which nixpkgs channel to use.
  channel = "stable-24.05";

  # Use https://search.nixos.org/packages to find packages
  packages = [
    pkgs.jdk11
    pkgs.maven
    pkgs.cloudflared
    pkgs.curl
  ];

  # Sets environment variables in the workspace
  env = {
    JAVA_HOME = "${pkgs.jdk11}/lib/openjdk";
  };

  idx = {
    # Search for the extensions you want on https://open-vsx.org/ and use "publisher.id"
    extensions = [
      "vscjava.vscode-java-pack"
      "vscjava.vscode-maven"
    ];

    # Workspace lifecycle hooks
    workspace = {
      # Runs when a workspace is first created
      onCreate = {
        build = "mvn clean package -DskipTests";
      };
      # Runs when the workspace is (re)started
      onStart = {
        start-server = "java -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -jar target/dependency/webapp-runner.jar --port 8081 target/ROOT.war &";
        start-tunnel = "sleep 15 && cloudflared tunnel --url http://127.0.0.1:8081";
      };
    };

    # Enable previews
    previews = {
      enable = true;
      previews = {
        web = {
          command = ["java" "-Dfile.encoding=UTF-8" "-jar" "target/dependency/webapp-runner.jar" "--port" "$PORT" "target/ROOT.war"];
          manager = "web";
          env = {
            PORT = "$PORT";
          };
        };
      };
    };
  };
}
