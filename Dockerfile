FROM openjdk:8-jre

FROM centurylink/wetty-cli:0.0.8
COPY --from=0 /usr/local/openjdk-8 /usr/local/openjdk-8
RUN ln -s /usr/local/openjdk-8/bin/java /usr/bin/java
RUN apt-get install -y curl zip

RUN curl -L https://github.com/fission/fission/releases/download/1.6.0/fission-cli-linux -o /usr/local/bin/fission
RUN chmod +x /usr/local/bin/fission

RUN curl 'https://d1vvhvl2y92vvt.cloudfront.net/awscli-exe-linux-x86_64.zip' -o 'awscli-exe.zip'
RUN unzip awscli-exe.zip
RUN aws/install
RUN ln -s /usr/local/aws-cli/v2/current/bin/aws2 /usr/bin/aws

#uncomment more for developer-aspect
RUN curl -L https://amazon-eks.s3-us-west-2.amazonaws.com/1.14.6/2019-08-22/bin/linux/amd64/kubectl -o /usr/local/bin/kubectl
RUN chmod +x /usr/local/bin/kubectl

# awscli
#RUN pip install awscli
COPY target/lingk-fission-cli-0.0.1-SNAPSHOT.jar /tmp/lingk-fission-cli.jar
RUN chmod +x /tmp/lingk-fission-cli.jar
RUN (echo '#!/usr/bin/java -jar'; cat /tmp/lingk-fission-cli.jar) > /usr/bin/lingk
RUN chmod +x /usr/bin/lingk



#uncomment more for developer-aspect
#RUN curl -L "https://github.com/weaveworks/eksctl/releases/download/latest_release/eksctl_$(uname -s)_amd64.tar.gz" | tar xz -C /tmp
#RUN mv /tmp/eksctl /usr/local/bin

#uncomment more for developer-aspect
#RUN curl -sLSf https://raw.githubusercontent.com/helm/helm/master/scripts/get | bash
