# Consommation d'une API REST

----

## Faire une requête HTTP
- avec le navigateur, via la barre d'adresse (uniquement requêtes GET)
- en utilisant des applications spécifiques telles que **Postman** ou **Advanced REST client**, qui sont liées au navigateur Chrome, ou **RESTClient** lié au navigateur Firefox
- avec la documentation interactive **Swagger** d'une API. Par exemple : `http://fakerestapi.azurewebsites.net/swagger/`
- en Java : par exemple, consommer une API dans son API ou dans son application web
- en JavaScript : consommer son API pour faire une IHM en JavaScript
- en bash : `curl https://jsonplaceholder.typicode.com/posts/1`
- en Libre Office
- en SAS
- etc...

----

## En JavaScript

- XMLHttpRequest
- Jquery
- Axios
- Fetch

----

## En Java

- sans bibliothèque en utilisant une **HttpURLConnection** et en lisant la réponse dans un **InputStream**, puis en utlisant **JAXB** pour lire les réponses XML et **Jackson** pour lire les réponses JSON
- avec des bibliothèques : OkHTTP, Jersey Client, Rest Template...

----

### Mise en place

Lecture du service :
```http
http://fakerestapi.azurewebsites.net/api/Users/1
```

réponse en JSON :
```json
{
    "ID": 1,
    "UserName": "User 1",
    "Password": "Password1"
}
```

réponse en XML :
```xml
<User xmlns:i="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://schemas.datacontract.org/2004/07/FakeRestAPI.Web.Models">
    <ID>1</ID>
    <Password>Password1</Password>
    <UserName>User 1</UserName>
</User>
```

----

Création d'un projet Maven en java 8 avec la dépendence **jackson-databind**, et création d'un Bean **User** :
```java
@XmlRootElement(name = "User")
@XmlAccessorType(XmlAccessType.FIELD)
public class User {

	@XmlElement(name = "ID")
	@JsonProperty("ID")
	private int id;
	@XmlElement(name = "UserName")
	@JsonProperty("UserName")
	private String userName;
	@XmlElement(name = "Password")
	@JsonProperty("Password")
	private String password;

    // getters, setters et méthode toString()
}
```

----

Création d'un fichier **package-info.java** pour lire la réponse XML :
```java
@XmlSchema(
    namespace="http://schemas.datacontract.org/2004/07/FakeRestAPI.Web.Models",
    elementFormDefault=XmlNsForm.QUALIFIED
)
package model;
 
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;
```

----

### Requête HTTP en GET sans bibliothèque

```java
System.setProperty("http.proxyHost", "proxy-rie.http.insee.fr");
System.setProperty("http.proxyPort", "8080");

// requête en GET avec réponse en XML
URL url = new URL("http://fakerestapi.azurewebsites.net/api/Users/1");
HttpURLConnection connection = (HttpURLConnection) url.openConnection();
connection.setRequestMethod("GET");
connection.setRequestProperty("Accept", "application/xml");
InputStream response = connection.getInputStream();
JAXBContext jc = JAXBContext.newInstance(User.class);
User user = (User) jc.createUnmarshaller().unmarshal(response);
connection.disconnect();
System.out.println(user);
System.out.println(connection.getResponseCode()); // 200
System.out.println(connection.getContentType()); // application/xml; charset=utf-8

// requête en GET avec réponse en JSON
URL url = new URL("http://fakerestapi.azurewebsites.net/api/Users/1");
HttpURLConnection connection = (HttpURLConnection) url.openConnection();
connection.setRequestMethod("GET");
connection.setRequestProperty("Accept", "application/json");
InputStream response = connection.getInputStream();
ObjectMapper mapper = new ObjectMapper();
User user = mapper.readValue(response, User.class);
connection.disconnect();
System.out.println(user);
```

----

## Requête HTTP en POST sans bibliothèque

```java
User user = new User(); user.setUserName("toto"); user.setPassword("azerty");
ObjectMapper mapper = new ObjectMapper();
URL url = new URL("http://fakerestapi.azurewebsites.net/api/Users");
HttpURLConnection connection = (HttpURLConnection) url.openConnection();
connection.setRequestMethod("POST");
connection.setRequestProperty("Accept", "application/json");
connection.setRequestProperty("Content-type", "application/json");
connection.setDoOutput(true); //this is to enable writing
mapper.writeValue(connection.getOutputStream(), user);

InputStream response = connection.getInputStream();
BufferedReader reader = new BufferedReader(new InputStreamReader(response,Charset.defaultCharset()));
String line = null;
while ((line=reader.readLine()) != null) {
	System.out.println(line);
}
connection.disconnect();
```


----

### Requête HTTP GET avec RestTemplate (1)

```xml
<dependency>
	<groupId>org.springframework</groupId>
	<artifactId>spring-web</artifactId>
	<version>5.1.6.RELEASE</version>
</dependency>
```

```java
String url = "http://fakerestapi.azurewebsites.net/api/Users/1";

System.out.println("Demande de la réponse sous forme de String (que l'on récupère au format JSON)");
RestTemplate restTemplate = new RestTemplate();
ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
System.out.println(response.getStatusCode()); // 200 OK
System.out.println(response.getBody()); // {"ID":1,"UserName":"User 1","Password":"Password1"}
System.out.println(response.getHeaders().getContentType()); // application/json;charset=utf-8
ObjectMapper mapper = new ObjectMapper();
User user = mapper.readValue(response.getBody(), User.class);
System.out.println(user); // User [id=1, userName=User 1, password=Password1]
System.out.println();
```

----

### Requête HTTP GET avec RestTemplate (2)

```java
System.out.println("Demande de la réponse sous forme de String au format XML");
HttpHeaders headers = new HttpHeaders();
headers.set("Accept", "application/xml");
HttpEntity<String> entity = new HttpEntity<String>(headers);
restTemplate = new RestTemplate();
response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
JAXBContext jc = JAXBContext.newInstance(User.class);
User userXml = (User) jc.createUnmarshaller().unmarshal(new StringReader(response.getBody()));
System.out.println(response.getStatusCode()); // 200 OK
System.out.println(response.getBody()); // <User xmlns ...
System.out.println(response.getHeaders().getContentType()); // application/xml;charset=utf-8
System.out.println(userXml); // User [id=1, userName=User 1, password=Password1]
System.out.println();

System.out.println("Demande de la réponse sous forme d'objet User");
restTemplate = new RestTemplate();
user = restTemplate.getForObject(url, User.class);
System.out.println(user);
```

----

### Requête HTTP POST avec RestTemplate

```java
String url = "http://fakerestapi.azurewebsites.net/api/Users";

User user = new User();
user.setUserName("toto");
user.setPassword("azerty");
System.out.println(user);

RestTemplate restTemplate = new RestTemplate();
HttpEntity<User> request = new HttpEntity<>(user);
User userResponse = restTemplate.postForObject(url, request, User.class);
System.out.println(userResponse);
```