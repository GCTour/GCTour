FROM scireum/sirius-runtime-jdk21:69
ADD --chown=sirius:sirius target/release-dir /home/sirius
WORKDIR /home/sirius
EXPOSE 9000
CMD ./run.sh
