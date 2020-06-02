# RPiCam-Desktop
RPiCam is a security camera infrastructure project that aims to connect commodity hardware like the Raspberry Pi and regular webcams into a unified network suitable for home and small business usage. In addition to providing live feed dashboards and footage archiving/retrieval, it will also use software techniques to do object and motion detection, allowing users to easily search for and be notified of potential security incidents like theft, trespassing, and tampering. 

RPiCam-Desktop serves as the central control and processing hub for this infrastructure.

## Building RPiCam
### Netbeans
Easiest way to build RPiCam currently is to just load up the project into Netbeans.
1. Install the [Java JDK](https://www.oracle.com/java/technologies/javase-downloads.html) (tested with Oracle JDK 14.0.1).
2. Install [Netbeans](https://netbeans.apache.org/) (tested with Netbeans 11.3 and 12.0beta5).
3. Open up the RPiCam-Desktop root folder as a project in Netbeans.
4. Click the Run button. Maven should handle the build dependencies for you.

### Build with Maven
1. Install the [Java JDK](https://www.oracle.com/java/technologies/javase-downloads.html) (tested with Oracle JDK 14.0.1).
2. Setup [Maven](https://maven.apache.org/).
3. `cd RPiCam-Desktop`
4. `mvn clean javafx:run`