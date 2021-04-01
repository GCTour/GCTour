FROM hub.scireum.com/scireum/sirius-runtime:29
COPY ./target/ /home/sirius
WORKDIR /home/sirius
EXPOSE 9000
CMD ./run.sh