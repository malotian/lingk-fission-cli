FROM openjdk:8-alpine

RUN set -ex && \
    apk --update add libstdc++ curl ca-certificates && \
    for pkg in glibc-2.28-r0 glibc-bin-2.28-r0; \
        do curl -sSL https://github.com/sgerrand/alpine-pkg-glibc/releases/download/2.28-r0/${pkg}.apk -o /tmp/${pkg}.apk; done && \
    apk add --allow-untrusted /tmp/*.apk && \
    rm -v /tmp/*.apk && \
    /usr/glibc-compat/sbin/ldconfig /lib /usr/glibc-compat/lib

RUN set -ex
RUN apk update
RUN apk add --update bash ca-certificates git python zip openssh
RUN apk add --update -t deps curl make py-pip openssl

COPY ../../../../target/lingk-fission-cli-0.0.1-SNAPSHOT.jar /tmp/lingk-fission-cli.jar
RUN chmod +x /tmp/lingk-fission-cli.jar
RUN (echo '#!/usr/bin/java -jar'; cat /tmp/lingk-fission-cli.jar) > /usr/bin/lingk
RUN chmod +x /usr/bin/lingk

RUN curl -L https://github.com/fission/fission/releases/download/1.6.0/fission-cli-linux -o /usr/local/bin/fission
RUN chmod +x /usr/local/bin/fission

#uncomment more for developer-aspect
RUN curl -L https://amazon-eks.s3-us-west-2.amazonaws.com/1.14.6/2019-08-22/bin/linux/amd64/kubectl -o /usr/local/bin/kubectl
RUN chmod +x /usr/local/bin/kubectl

# awscli
RUN pip install awscli yamllint yq

#uncomment more for developer-aspect
#RUN curl -L "https://github.com/weaveworks/eksctl/releases/download/latest_release/eksctl_$(uname -s)_amd64.tar.gz" | tar xz -C /tmp
#RUN mv /tmp/eksctl /usr/local/bin

#uncomment more for developer-aspect
#RUN curl -sLSf https://raw.githubusercontent.com/helm/helm/master/scripts/get | bash
