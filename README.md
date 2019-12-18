# Natural Language Processing As A Service

The NLP-AAS project provides an asynchronous HTTP API for the [Language Application Grid's SOAP services](https://api.lappsgrid.org/services).

## Calling Services

To invoke a service POST a JSON document to https://api.lappsgrid.org/nlpaas/submit.  The format of the JSON document is:

```json
{
  "services": [ "service_id_1", "service_id_2", ... , "service_id_N" ],
  "type" : "text/plain",
  "payload" : "The text to process."
}
```

Where:

1. *services* is a list of IDs of the services to be invoked.  You can obtain a list of the services and their service IDs from the [LAPPS Grid services page]((https://api.lappsgrid.org/services)). There are also [shortcuts](#shortcuts-for-common-services) for commonly used services from [Stanford CoreNLP](https://stanfordnlp.github.io/CoreNLP/), [Apache OpenNLP](https://opennlp.apache.org), [Lingpipe](http://alias-i.com/index.html), and [GATE](https://gate.ac.uk).
1. *type* the format of the data being POSTed.  This **must** be one of *text/plain* or *application/json*.  If the type is *application/json* then the payload **must** be a LAPPS Data object. 
1. *payload* the data to be processed

The server will respond with a *201 CREATED* message with a *Location* header in the response that provides the URL to use to check the status of the job submission.

``` 
$ curl -i -H "content-type: application/json" -d @request.json https://api.lappsgrid.org/nlpaas/submit
HTTP/1.1 201 
Location: /job/910ca618-a78c-40eb-9673-3ffa24a3fb1f
Content-Length: 0
Date: Mon, 16 Dec 2019 18:36:15 GMT

```

Use the */job* URL to obtain information about the submitted job including:

1. The job status (IN_QUEUE, IN_PROGRESS, DONE, or ERROR).
1. The time (UTC) the job was submitted.
1. The time processing started.
1. The time processing finished (if successful).
1. The time processing was halted due to an error.
1. The total elapsed processing time (in milliseconds).
1. An error message, if any, and
1. The URL of the download file, if ready and available.

``` 
$ curl -i https://api.lappsgrid.org/nlpaas/job/910ca618-a78c-40eb-9673-3ffa24a3fb1f
HTTP/1.1 200 
Content-Type: application/json;charset=UTF-8
Transfer-Encoding: chunked
Date: Mon, 16 Dec 2019 18:41:42 GMT

{
    "id": "910ca618-a78c-40eb-9673-3ffa24a3fb1f",
    "elapsed": 2029,
    "status": "DONE",
    "submitted_at": "2019-12-16T18:36:15.808Z",
    "started_at": "2019-12-16T18:36:15.812Z",
    "finished_at": "2019-12-16T18:36:17.841Z",
    "result_URL": "/download/910ca618-a78c-40eb-9673-3ffa24a3fb1f"
}
```

Send a *GET* request to the *result_URL* to download the processed document.  Files will be available for download for 30 minutes after processing has completed.

## Validating a Pipeline of Servives

To ensure that a series of service is valid, that is, the input requirements of each service have been satisfied, send the job request to https://api.lappsgrid.org/nlpaas/validate

``` 
$ curl -i -H "content-type: application/json" -d @request.json https://api.lappsgrid.org/nlpaas/validate 
HTTP/1.1 100 

HTTP/1.1 200 
Content-Type: application/json;charset=UTF-8
Transfer-Encoding: chunked
Date: Mon, 16 Dec 2019 18:55:57 GMT

{"status":200,"message":"OK"}

$ curl -i -H "content-type: application/json" -d @missing-annotations.json https://api.lappsgrid.org/nlpaas/validate 
HTTP/1.1 400 
Content-Type: application/json;charset=UTF-8
Transfer-Encoding: chunked
Date: Tue, 17 Dec 2019 18:09:15 GMT
Connection: close

{
    "status": 400,
    "message": "Required annotations are missing",
    "reason": "anc:opennlp.cloud.tokenizer_1.0.0 requires http://vocab.lappsgrid.org/Sentence"
}
```

## Shortcuts For Common Services

Shortcut IDs have been created for the most commonly used services from Stanford CoreNLP, Apache OpenNLP, and Lingpipe,

<table>
    <tr>
    <th>&nbsp;</th>
    <th>Stanford</th>
    <th>OpenNLP</th>
    <th>Lingpipe</th>
    <th>Gate</th>
    </tr>
    <tr>
        <td>Tokenizer</td>
        <td>stanford.tokenizer</td>
        <td>opennlp.tokenizer</td>
        <td>lingpipe.tokenizer</td>
        <td>gate.tokenizer</td>
    </tr>
    <tr>
        <td>Sentence Segmenter</td>
        <td>stanford.splitter</td>
        <td>opennlp.splitter</td>
        <td>lingpipe.splitter</td>
        <td>gate.splitter</td>
    </tr>
    <tr>
        <td>POS Tagger</td>
        <td>stanford.tagger</td>
        <td>opennlp.tagger</td>
        <td>lingpipe.tagger</td>
        <td>gate.tagger
    </tr>
    <tr>
        <td>Lemmatizer</td>
        <td>N/A</td>
        <td>opennlp.lemmatizer</td>
        <td>N/A</td>
        <td>N/A</td>
    </tr>
    <tr>
        <td>Named Entity Recognizer</td>
        <td>stanford.ner</td>
        <td>opennlp.ner</td>
        <td>lingpipe.ner</td>
        <td>gate.ner</td>
    </tr>
</table>

```json
{
  "services" : [ 
    "stanford.tokenizer", 
    "gate.splitter",
    "opennlp.tagger",
    "opennlp.lemmatizer", 
    "lingpipe.ner" 
  ],
  "type" : "text/plain",
  "payload": "Hello world."
}
```

## Pre-configured Pipelines

There are two preconfigure pipelines that can be used for Named Entity Recognition:

1. stanford.ner.pipeline
1. opennlp.ner.pipeline

There are no pre-configured GATE or Lingpipe NER pipelines at this time.

## Finding Services

To find the available services that are capable of producing a given annotation type send a *GET* request to `https://api.lappsgrid.org/nlpaas/producers?type={type}`

``` 
$ curl -i http://api.lappsgrid.org/nlpaas/producers?type=token%23lemma
HTTP/1.1 200 
Content-Type: application/json;charset=UTF-8
Transfer-Encoding: chunked
Date: Tue, 17 Dec 2019 00:55:26 GMT

{
    "status": 200,
    "producers": [
        "anc:opennlp.cloud.lemmatizer_pipeline_1.0.0",
        "anc:opennlp.cloud.ner_pipeline_1.0.0",
        "anc:opennlp.cloud.pipeline_1.0.0",
        "anc:opennlp.cloud.lemmatizer_1.0.0",
        "anc:stanford.cloud.ner_1.0.0",
        "anc:stanford.cloud.lemmatizer_1.0.0",
        "anc:gost_1.0.0-SNAPSHOT"
    ]
}
```

## Finding Information About Services

Each service in the LAPPS Grid returns metadata about the input formats in accepts, the annotations it produces, the annotations it requires in its input, licensing terms, and much more.  To obtain metadata about a service use the `/metadata?id={id}` end point.

```
$ curl -i https://api.lappsgrid.org/nlpaas/metadata?id=opennlp.ner
HTTP/1.1 200 
Content-Type: application/json
Content-Length: 859
Date: Tue, 17 Dec 2019 00:59:35 GMT

{
  "discriminator" : "http://vocab.lappsgrid.org/ns/meta",
  "payload" : {
    "$schema" : "https://vocab.lappsgrid.org/schema/1.1.0/metadata-schema.json",
    "name" : "org.lappsgrid.cloud.opennlp.soap.NamedEntityRecognizer:1.0.0-SNAPSHOT",
    "version" : "1.0.0-SNAPSHOT",
    "toolVersion" : "1.9.1",
    "description" : "Apache OpenNLP Named Entity Recognizer",
    "allow" : "http://vocab.lappsgrid.org/ns/allow#any",
    "license" : "http://vocab.lappsgrid.org/ns/license#apache-2.0",
    "requires" : {
      "format" : [ "http://vocab.lappsgrid.org/ns/media/jsonld#lif" ],
      "annotations" : [ "http://vocab.lappsgrid.org/Token", "http://vocab.lappsgrid.org/Token#pos" ]
    },
    "produces" : {
      "format" : [ "http://vocab.lappsgrid.org/ns/media/jsonld#lif" ],
      "annotations" : [ "http://vocab.lappsgrid.org/NamedEntity" ]
    }
  }
}
```
