FROM hub.scireum.com/scireum/sirius-runtime:29
ADD --chown=sirius:sirius target/release-dir /home/sirius
WORKDIR /home/sirius
EXPOSE 9000
CMD ./run.sh