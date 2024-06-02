# RAD Source

This is a fork of [rad-source](https://github.com/cloudhubs/rad-source) repository used to create the Temporal SDG dataset.

This project detects the REST endpoints and clients (API calls) using static source code analysis for JAVA Spring Boot projects.
For multiple microservices, it also finds the rest communication between the microservices. 

## Major Dependencies

- [Spring boot](https://spring.io/projects/spring-boot)
- [Java parser](https://github.com/javaparser/javaparser)
- [Lombok](https://projectlombok.org/)

## Core Components

1. **RadSourceService:** Takes a list of paths of a single JAVA source file or, a directory as input. If input is a directory then it scans all java source files recursively and for each of them runs `RestCallService` and `RestEndpointService`.

2. **RestCallService:** Takes a single JAVA source file as input and detects all the rest calls along with their parent method, HTTP type, and return type.

3. **RestEndpointService:** Takes a single JAVA source file as input and detects all the rest endpoints along with their parent method, HTTP type, and return type.

4. **RestFlowService:** Takes a list of `RestCall` and `RestEndpoint` as input and matches them to detect rest communication between microservices.

## Run the Application

### Compile and run

```
$ mvn clean install -DskipTests
$ java -jar application/target/rad-source-application-0.0.5.jar
```

### Sample request and response

You can either use a single java source file path or a directory path in `pathToSource`.

```
$ curl --request POST \
    --url http://localhost:8080/ \
    --header 'content-type: application/json' \
    --data '{
      "pathToMsRoots": [PATH_HERE]
  }'
```

```
{
  "request": {
    "pathToMsRoots": [
      "C:\\seer-lab\\cil-tms\\tms-cms",
      "C:\\seer-lab\\cil-tms\\tms-ems"
    ]
  },
  "restEntityContexts": [
    {
      "pathToMsRoot": "C:\\seer-lab\\cil-tms\\tms-cms",
      "restCalls": [
        {
          "msRoot": "C:\\seer-lab\\cil-tms\\tms-cms",
          "source": "C:\\seer-lab\\cil-tms\\tms-cms\\src\\main\\java\\edu\\baylor\\ecs\\cms\\service\\EmsService.java",
          "httpMethod": "POST",
          "parentMethod": "edu.baylor.ecs.cms.service.EmsService.createExam",
          "returnType": "edu.baylor.ecs.cms.dto.ExamDto",
          "collection": false
        },
        ...
      ],
      "restEndpoints": [
        {
          "msRoot": "C:\\seer-lab\\cil-tms\\tms-cms",
          "source": "C:\\seer-lab\\cil-tms\\tms-cms\\src\\main\\java\\edu\\baylor\\ecs\\cms\\controller\\CategoryInfoController.java",
          "httpMethod": "GET",
          "parentMethod": "edu.baylor.ecs.cms.controller.CategoryInfoController.getCategoryInfo",
          "arguments": "[]",
          "returnType": "java.lang.Object",
          "collection": true
        },
        ...
      ]
    },
    ...
  ],
  "restFlows": [
    {
      "client": {
        "msRoot": "C:\\seer-lab\\cil-tms\\tms-cms",
        "source": "C:\\seer-lab\\cil-tms\\tms-cms\\src\\main\\java\\edu\\baylor\\ecs\\cms\\service\\EmsService.java",
        "httpMethod": "GET",
        "parentMethod": "edu.baylor.ecs.cms.service.EmsService.getQuestionsForExam",
        "returnType": "edu.baylor.ecs.cms.model.Question",
        "collection": true
      },
      "endpoint": {
        "msRoot": "C:\\seer-lab\\cil-tms\\tms-ems",
        "source": "C:\\seer-lab\\cil-tms\\tms-ems\\src\\main\\java\\edu\\baylor\\ecs\\ems\\controller\\ExamController.java",
        "httpMethod": "GET",
        "parentMethod": "edu.baylor.ecs.ems.controller.ExamController.listAllQuestionsForExam",
        "arguments": "[@PathVariable Integer id]",
        "returnType": "edu.baylor.ecs.ems.model.Question",
        "collection": true
      }
    },
    ...
  ]
}
```
