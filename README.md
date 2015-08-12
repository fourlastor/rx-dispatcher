# rx-dispatcher (WIP)
An asynchronous request dispatcher for testing http calls.

This is a request handler for [RxNetty](https://github.com/ReactiveX/RxNetty/) that will hang requests until you set a response.

```java
RxDispatcher rxDispatcher = new RxDispatcher();
HttpServer<ByteBuf, ByteBuf> server = RxNetty.createHttpServer(8080, rxDispatcher);
server.start();

// do request to http://localhost:8080/hello - it will hang
rxDispatcher.match("/hello", "world");
// the server will respond to the request previously made with "hello"
```

## Installation

Add the correct repository to your gradle file

```
repositories {
    jcenter()
}

dependencies {
    androidTestCompile 'com.fourlastor:rx-dispatcher:0.0.1'
}
```

## Motivation

Writing Android instrumentation tests was quite difficoult with [MockWebServer](https://github.com/square/okhttp/tree/master/mockwebserver) because the activity would start (and do the requests) **before** the test even started. That's especially true if you use the new `@Rule` system.

One workaround is to extend the old `ActivityInstrumentationTestCase2` and delay `getActivity()` after you setup your calls with `MockWebServer`. I found this approach complicated and not clean.

Inspired by [RxPresso](https://github.com/novoda/rxpresso/) I wrote this. I understood the motivations behind RxPresso but don't agree in mocking parts of the application in an end to end test.

Writing a test now will be as simple as this:

```java
@RunWith(AndroidJUnit4.class)
public class MyCoolActivityTest {
    private static int PORT = 8080;
    private RxDispatcher rxDispatcher;
    private HttpServer<ByteBuf, ByteBuf> server;
    private RxDispatcher rxDispatcher;
    
    @Rule
    public ActivityTestRule<MyCoolActivity> rule = new ActivityTestRule<MyCoolActivity>(MyCoolActivity.class) {
        @Override
        protected void beforeActivityLaunched() {
          rxDispatcher = new RxDispatcher();
          server = RxNetty.createHttpServer(PORT, rxDispatcher);
          server.start();
          // change your app settings to use http://localhost:8080 as the endpoint
        }

        @Override
        protected void afterActivityFinished() {
          server.shutdown();
        }
    };

    @Test
    public void helloWorld() throws Exception {
      rxDispatcher.match("/hello", "world");
      onView(withId(R.id.target_for_hello))
            .check(matches(withText("world")));
    }
}
```
