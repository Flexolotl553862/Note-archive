FROM neowu/jre:24.0.1
WORKDIR /note-archive
COPY note-archive-0.0.1-SNAPSHOT.jar ./note-archive.jar
COPY note-archive_app-db/_data/note-archive-db.mv.db ./db/note-archive-db.mv.db
COPY note-archive_app-certs/_data/keystore.p12 ./certs/keystore.p12
COPY note-archive_app-data/_data ./data
ENTRYPOINT ["java", "-Duser.timezone=Europe/Moscow", "-jar", "note-archive.jar"]