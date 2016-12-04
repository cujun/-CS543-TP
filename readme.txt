

How to install

## You can download client and server folders from https://github.com/cujun/CS543_HISS
<Client>
1. First, confirm that you have two cloudinary libraries(extension '.jar') in "client/org.eclipse.paho.android.sample/libs/".
2. Run AndroidStudio(Android IDE), then import project(or open) by choosing 'client' folder. (Note that our android project is gradle project)
3. Build 'org.eclipse.paho.android.sample' modules, neither 'org.~~.service' nor 'paho.~~.sample'
4. Execute run command (If there are some errors, clean and rebuild whole project)
<Server>
* Please refer to below sites
* https://www.digitalocean.com/community/questions/how-to-setup-a-mosquitto-mqtt-server-and-receive-data-from-owntrack
* http://mobicon.tistory.com/12s


How to run

<Cleint>
* Publish,
    Run the application and click 'PUBLISH' tab along the top of start page.
    Select the image you want to publish with the hashtag(start with '#').
    Just click 'PUBLISH' button.
* Subscribe,
    Run the application and click 'SUBSCRIBE' tab along the top of start page.
    If you want to subscribe new topic, click 'NEW SUBSCRIPTION' button and enter the topic(not include '#') you want to subscribe. Then click 'OK' button.
    If you want to unsubscribe, click on the trash can button in the subscription you want to delete.


<Server>
* Just use 'mosquitto' command


