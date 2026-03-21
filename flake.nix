{
  description = "Devshell for Expo/React Native apps";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs {
          inherit system;
          config.allowUnfree = true;
          config.android_sdk.accept_license = true;
        };
        androidSdk = pkgs.androidenv.composeAndroidPackages {
          buildToolsVersions = [ "35.0.0" "36.0.0" ];
          platformVersions = [ "35" "36" ];
          ndkVersions = [ "27.1.12297006" ];
          cmakeVersions = [ "3.22.1" ];
          includeEmulator = false;
          includeSystemImages = false;
          includeSources = false;
          includeNDK = true;
        };
        sdk = androidSdk.androidsdk;
      in
      {
        devShells.default = pkgs.mkShell {
          buildInputs = with pkgs; [
            bun
            nodejs_22
            nodePackages.eas-cli
            jdk17
            android-tools
            sdk
            python3
            gnumake
            gcc
            util-linux
          ];

          GRADLE_OPTS = "-Dorg.gradle.project.android.aapt2FromMavenOverride=${sdk}/libexec/android-sdk/build-tools/35.0.0/aapt2";

          shellHook = ''
            export JAVA_HOME="${pkgs.jdk17}"
            export LD_LIBRARY_PATH="${pkgs.util-linux.lib}/lib:$LD_LIBRARY_PATH"
            export ANDROID_HOME="${sdk}/libexec/android-sdk"
            export ANDROID_SDK_ROOT="$ANDROID_HOME"
            export PATH="$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH"
            echo "Dev shell for Expo/React Native apps"
            echo "  bun install   - install dependencies"
            echo "  bun start     - start dev server"
            echo "  bunx expo run:android - build and run on Android"
          '';
        };
      });
}
