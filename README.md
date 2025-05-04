### System overview and scope
**System overview**
A URL shortener is a web-based service that converts long URLs into substantially shorter alternatives that redirect to the original URL when accesses.
**Purpose of the system**
Primary purpose is to transform legthy, complex URLs into concise, easy-to-share links. In my design i included only main components and functions and think out of scope ffunctionality is not really interesting.
link to implementation 

My application is written in Java Spring, to start application you need to import project and download needed JDK i use Java versioin 17
Then in docker folder you need to run init.sh file that will create images of microservices and start needed containers

In repository there is json file to post request, you can import it in  for shortening long URL. 
After you make POST request to server by this GET request http://localhost/api/shortUrl/{hashValue} you can access to it, by changing hashValue to what was returned to you
