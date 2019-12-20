ROOT=$(shell pwd)
JAR=nlpaas.jar
REPO=docker.lappsgrid.org
GROUP=lappsgrid
DOCKER=src/main/docker
NAME=nlpaas
IMAGE=$(GROUP)/$(NAME)
TARGET=target/$(JAR)
TAG=$(REPO)/$(IMAGE)

jar:
	mvn package

clean:
	mvn clean
	if [ -e $(DOCKER)/$(JAR) ] ; then rm $(DOCKER)/$(JAR) ; fi
 
run:
	java -jar $(TARGET)

docker:
	if [ ! -e $(DOCKER)/$(JAR) ] ; then cp $(TARGET) $(DOCKER) ; fi
	if [ $(TARGET) -nt $(DOCKER)/$(JAR) ] ; then cp $(TARGET) $(DOCKER) ; fi
	cd $(DOCKER) && docker build -t $(IMAGE) .

start:
	docker run -d -p 8080:8080 --name $(NAME) $(IMAGE)

stop:
	docker rm -f $(NAME)

push:
	docker tag $(IMAGE) $(TAG)
	docker push $(TAG)

update:
	curl -i http://129.114.17.83:9000/api/webhooks/9e3e277e-3cab-4d25-a1f4-65adf17a8477
	