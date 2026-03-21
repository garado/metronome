{
  description = "Devshell for Expo/React Native apps";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = nixpkgs.legacyPackages.${system};
      in
      {
        devShells.default = pkgs.mkShell {
          buildInputs = with pkgs; [
            bun
            nodejs_22

            # Android build dependencies
            jdk17
            android-tools

            # Native build tools (for node modules with native bindings)
            python3
            gnumake
            gcc
          ];

          shellHook = ''
            export JAVA_HOME="${pkgs.jdk17}"
            echo "Dev shell for Expo/React Native apps"
            echo "  bun install   - install dependencies"
            echo "  bun start     - start dev server"
            echo "  bunx expo run:android - build and run on Android"
          '';
        };
      });
}
