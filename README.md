# RxNetwork

[![Build Status][circleci-shield]][circleci-link]
[![Codecov][codecov-shield]][codecov-link]
[![Maven Release][maven-shield]][maven-link]
[![License][license-shield]][license-link]

Reactive Android library with easy-to-use API for observing network connectivity and internet access.

Library is compatible with **RxJava 2.x** and **RxAndroid 2.x** and can be included in any Android
application that supports **minSdk = 9** (`Android 2.3 Gingerbread` and up)    

## Introduction

RxNetwork is a re-hash of popular [Reactive Network](https://github.com/pwittchen/ReactiveNetwork)
library. It uses distinctively different architectural approach when compared to the original 
while at the same time it tries to leave what was already familiar. 

Initial idea was to bring original library up to speed so it could be used with RxJava2. 
[Piotr](https://github.com/pwittchen) - the original author was already in the process of making 
this possible but it was going a little slow for our taste and looked like his *2x branch* was stuck
a little. We decided to fork it but somewhere along the way, as we tried to tailor it to our own 
projects and code practices, it reshaped so much that we realized under the hood it was already 
[quite different](#whats-different) beast.  

Here it is then. In the spirit of the original library, we are releasing it into the open as well 
just to give you, the developer, an option. Use whatever approach you find suitable.

## Content

- [What's different](#whats-different)
- [Usage](#usage)
- [Configuration](#configuration)
  - [Initialization](#initialization)
  - [Advanced configuration](#advanced-configuration)
    - [Default scheduler](#default-scheduler)
    - [Custom strategies](#custom-strategies)
    - [Custom factories](#custom-factories)
- [Observing network connectivity](#observing-network-connectivity)
  - [RxNetworkInfo](#rxnetworkinfo)
  - [Observing](#observing)
  - [Simple observing](#simple-observing)
  - [Observing with custom strategy](#observing-with-custom-strategy)
  - [Observing with NetworkCapabilities](#observing-with-networkcapabilities)
  - [Filtering](#filtering)
  - [Things to consider](#things-to-consider)
- [Observing true internet access](#observing-true-internet-access)
  - [Observing internet access](#observing-internet-access)
  - [Built-in internet observing strategies](#built-in-internet-observing-strategies)
- [Examples](#examples)
- [Tests](#tests)
- [Code style](#code-style)
- [Contributing](#contributing)
- [Changelog](#changelog)
- [Credits](#credits)
- [License](#license)

## What's different 

Here's the short list of what has changed compared to the original library:

1. Instead of `static utility class` approach we decided on the `instance` one. We strongly believe 
it's healthier for testing and that ultimately it should be up to end developer to choose how 
to instantiate the main class. You may do it on the fly, you may use some kind of DI approach, be 
it manual, or something like Google's [Dagger2](https://github.com/google/dagger). Or better yet 
**our favourite**: [Toothpick](https://github.com/stephanenicolas/toothpick). It's your call 
ultimately. If you want a single instance for your whole project - you got it. If you want couple of 
differently configured one's for different part of your project - you can do that too. You decide.

2. **We swept out ominous** `Context` **dangling around in the original class**. We wanted cleaner 
API so we decided to get it out of our way when it's not truly needed. So for you as well: no more 
passing it even if your observing strategy doesn't require it. Read on the [usage](#usage) section 
to see for yourself or look it up how it's done in the code.

3. Most of the classes now use `Builder Pattern` for configuration. The motto is: **use only what 
you what you truly need**.   

4. **`Application` context everywhere**. This one is mostly **due to memory leak** as reported in 
issue [#43945](https://code.google.com/p/android/issues/detail?id=43945) in Android issue tracker 
(and mentioned by Piotr as well). The problem, originally, was supposed to occur only in 
Android 4.2, but many report it as still persistent on 5.1 devices and so on. We decided to nip 
the problem in the butt and leave no door open. Hence our strong stand on this change. Remember 
that this will only affect you when initializing RxNetwork library in your application. No worries 
though - you can still do it directly within activities/fragments if you decide to.

5. For <b>API 21+</b>: enhanced network information with [NetworkCapabilities](https://developer.android.com/reference/android/net/NetworkCapabilities.html)
and configurable default [NetworkRequest](https://developer.android.com/reference/android/net/NetworkRequest.html)

6. For **checking true internet access** our default internet observing strategy implementation 
(see [Built-in internet observing strategies](#built-in-internet-observing-strategies)) uses 
`URLConnection` instead of socket-based approach proposed by original library. It also **takes care 
of captive portals / walled-garden internet scenarios that was mentioned by many as problematic**. 
 
7. We also decided to include couple of other already tested and ready to go internet observing 
strategies to enhance your observing possibilities. Right now there are three of them: 
  - [`WalledGardenInternetObservingStrategy`](https://github.com/greyfoxit/RxNetwork/blob/master/rxnetwork/src/main/java/greyfox/rxnetwork/internal/strategy/internet/impl/WalledGardenInternetObservingStrategy.java) (library's default)
  - [`HttpOkInternetObservingStrategy`](https://github.com/greyfoxit/RxNetwork/blob/master/rxnetwork/src/main/java/greyfox/rxnetwork/internal/strategy/internet/impl/HttpOkInternetObservingStrategy.java) 
  - [`SocketInternetObservingStrategy`](https://github.com/greyfoxit/RxNetwork/blob/master/rxnetwork/src/main/java/greyfox/rxnetwork/internal/strategy/internet/impl/SocketInternetObservingStrategy.java) 

  Instead of polluted, multi-param methods all strategies are now fully configurable via 
  `Builder Pattern` to give you cleaner interface to work with.

8. New predicates for [filtering RxNetwork's observables](filtering).

9. <i>Tests, tests, tests</i>. And meaningful ones. We tried to do our best to thoroughly check 
RxNetwork so you could (hopefully) use it worry-free in your own projects from the get-go.

## Usage

Download [the latest JAR][jar] or grab via Maven:
```xml
<dependency>
  <groupId>it.greyfox</groupId>
  <artifactId>rxnetwork</artifactId>
  <version>0.0.5/version>
</dependency>
```
or Gradle:
```groovy
compile 'it.greyfox:rxnetwork:0.0.5'
```

Snapshots of the development version are available in [Sonatype's `snapshots` repository][snap].

[jar]: https://search.maven.org/remote_content?g=it.greyfox&a=rxnetwork&v=LATEST
[snap]: https://oss.sonatype.org/content/repositories/snapshots/it/greyfox/rxnetwork

## Configuration

[`RxNetwork`](https://github.com/greyfoxit/RxNetwork/blob/master/rxnetwork/src/main/java/greyfox/rxnetwork/RxNetwork.java) 
is the main class via which you can subscribe to available observables. By default library tries to 
give you sane defaults, but it allows for customization (see [Advanced configuration](#advanced-configuration) section).

### Initialization

In your `Application` class:

```java
public class ExampleApplication extends Application {
    
    @Override public void onCreate() {
        super.onCreate();
        
        RxNetwork.init(this); // this is the line
    }
}
```

**That's it. You're good to go!**

RxNetwork will automatically set up all the things for you. By default library, via its provider 
mechanism, will choose appropriate, API-specific strategy for observing network connectivity. This 
is so it could support both new and legacy network monitoring strategies based on concrete version. 

For observing true internet access RxNetwork defaults to [`WalledGardenInternetObservingStrategy`](https://github.com/greyfoxit/RxNetwork/blob/master/rxnetwork/src/main/java/greyfox/rxnetwork/internal/strategy/internet/impl/WalledGardenInternetObservingStrategy.java)
partially described earlier in *What's different* and more in [Built-in internet observing strategies](#built-in-internet-observing-strategies) 

**Ok, seems easy enough, but what is truly going on here?** 

Well, the main idea is that `init` **method will return configured instance of** `RxNetwork`. 
It's up to you what you'll do with it. 

Say you'd like, for example, to centralize your connectivity information in one place and pass that 
information around somehow (maybe you're using *event bus* or whatever). `Application` as an entry 
point would seem to be a good place for that. You could simply use RxNetwork like this:

```java
public class ExampleApplication extends Application {

    @Override public void onCreate() {
        super.onCreate();
        
        RxNetwork.init(this).observe()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribeWith(new YourCustomObserver());
    }
}
```

Or maybe you would like to take an instance (you can have many differently configured instances)

```java
RxNetwork rxnetwork = RxNetwork.init(this);
```

and make it a dependency somewhere (preferably with some kind of DI) 

```java
public class ExampleActivity extends Activity {
    
    @Inject public RxNetwork rxNetwork;
    
    (...)
    
    @Override
    protected void onResume() {
        super.onResume();
        
        rxNetwork.observe()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribeWith(new YourCustomObserver());
    }
}
```

*And remember*: this doesn't necessarily have to be an `Activity` or `Fragment`. Without `Context` 
laying around in observing methods (or without the headache of dragging it along as a chaperon)
*it can be anywhere* 

The **possibility** though **of simple inlining** within any context-aware place **still exists**:

```java
import android.support.v4.app.Fragment;

public class ExampleFragment extends Fragment {

    @Override
    public void onResume() {
        super.onResume();
        
        RxNetwork.init(getContext()).observe()
            .subscribe(...)
    }
}
```

but we must say that we advise against it and strongly recommend DI approach - if not only for the
sake of your testing (which we hope you do) 

**Lastly**, there is one more convenience: 

**you can omit passing** `Context` entirely when initializing RxNetwork  **if you decide you'll be 
observing real internet access only or when providing your own network observing strategy factory**. 
Just use an empty `RxNetwork.init()` and be done with it. Bare in mind though you won't be able to 
use built-in network observing methods unless you pass network observing strategy ad-hoc or via 
factory or initialize RxNetwork with proper `Context`

Too see exemplary application (with DI, Retrolambda and all) **check out** `app` directory 
in the repo.

### Advanced configuration

#### Default scheduler

With RxNetwork **you can pass default** `Scheduler` **instance on which you want all of your 
observable subscriptions to be executed**. This is just simple convenience code so you don't have to 
put `subscribeOn()` in every call

```java
RxNetwork.builder().defaultScheduler(Scheduler.io()).init(this);
```

Of course **you can still choose to use specific** `Scheduler` instance **on individual observable 
subscription**. This will override provided default for that call while still leaving it for others

#### Custom strategies

It is perfectly possible that for your observing needs you would want to use your own strategies
as your defaults. For that you can use following builder methods to set up RxNetwork library 
for your own implementations:

```java
RxNetwork.builder()
    .networkObservingStrategy(new YourCustomNetworkObservingStrategy())
    .internetObservingStrategy(new YourCustomInternetObservingStrategy())
    
    (...)
``` 

#### Custom factories

Perhaps you decide to tinker even more with initial configuration. Say you would like to use 
your own factory that in turn would take care of picking up concrete strategy based on some 
provision mechanism of your own (this is exactly what RxNetwork does under the hood). Well, good 
news. With RxNetwork this is possible as well. Just do as below

for network observing strategy:

```java
RxNetwork.builder().networkObservingStrategyFactory(new YourCustomNetworkObservingStrategyFactory())
```

and for the sake of code symmetry there is also one for internet observing strategy:

```java
RxNetwork.builder().internetObservingStrategyFactory(new YourCustomInternetObservingStrategyFactory())
```

Right now you're probably wondering: *what are all those different classes ?* Well, we're glad you 
asked. It's simple. Just the couple of basic, self explanatory interfaces you need to implement in 
case you'd like to use your own observing strategies. If you want you can have a look:

- [`NetworkObservingStrategy`](https://github.com/greyfoxit/RxNetwork/blob/master/rxnetwork/src/main/java/greyfox/rxnetwork/internal/strategy/network/NetworkObservingStrategy.java)
- [`NetworkObservingStrategyFactory`](https://github.com/greyfoxit/RxNetwork/blob/master/rxnetwork/src/main/java/greyfox/rxnetwork/internal/strategy/network/NetworkObservingStrategyFactory.java)
- [`InternetObservingStrategy`](https://github.com/greyfoxit/RxNetwork/blob/master/rxnetwork/src/main/java/greyfox/rxnetwork/internal/strategy/network/InternetObservingStrategy.java)
- [`InternetObservingStrategyFactory`](https://github.com/greyfoxit/RxNetwork/blob/master/rxnetwork/src/main/java/greyfox/rxnetwork/internal/strategy/internet/InternetObservingStrategyFactory.java)

but other than that, in most situations, you can forget it all and roll happily with defaults 
without all the fuss.

### Observing network connectivity

Now, let's see what we can *really* do with this library. First let's take a look at

#### RxNetworkInfo

[`RxNetworkInfo`](https://github.com/greyfoxit/RxNetwork/blob/master/rxnetwork/src/main/java/greyfox/rxnetwork/internal/net/RxNetworkInfo.java) 
is *the* class that you would use if you decide you want more than just basic information about 
the network connection. This class is simply a wrapper around Android's original `NetworkInfo` 
that you can use to extract all the original info. Starting from *Lollipop* (`API 21+`) 
it also provides additional `NetworkCapabilities` information.

#### Observing

You can observe network connectivity with `observe` method. In your class this could look like this:

```java
@Inject RxNetwork rxNetwork;

(...)

rxNetwork.observe()
    .observeOn(AndroidSchedulers.mainThread())
    .subscribeOn(Schedulers.io())
    .subscribe(new Consumer<RxNetworkInfo>() {
        @Override
        public void accept(RxNetworkInfo networkInfo) throws Exception {
            // do sth with networkInfo like calling getState(), getType(), isConnected()
            // and so on (essentialy anything you'd normally do with NetworkInfo)
        }
    }
);
```

or sth shorter like this:  

```java
rxNetwork.observe()
    .observeOn(AndroidSchedulers.mainThread())
    .subscribeOn(Schedulers.io())
    .subscribeWith(new YourCustomObserver());
```

**If you're using Retrolambda** like us (or Android's new improved support for Java 8 language features 
built into the default toolchain), and decide to [set up default `Scheduler`](#default-scheduler) 
on RxNetwork **this could go down** to this: 

```java
rxNetwork.observe()
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe(networkInfo -> {
        // do sth with networkInfo
    }
);
```

for example:

```java
rxNetwork.observe()
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe(networkInfo -> System.out.println("Connected: " + networkInfo.isConnected()));
```

**We could go even further and prettier** with method references, let's say:

```java
rxNetwork.observe()
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe(this::onNetworkInfo);
         
void onNetworkInfo(RxNetworkInfo networkInfo) {
    // do sth with networkInfo 
}      
```
The latest can be expanded with regular RxJava2 `subscribe` to take care of errors of course:

```java
rxNetwork.observe()
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe(this::onNetworkInfo, this::onError);
         
(...)
```

This is just to give you idea of what's possible. From here forward we'll assume default `Scheduler` 
has been already set and we'll be using lambdas and method references whenever possible for the sake 
of brevity.

#### Simple observing

If you only care about simple ***true / false*** information about the network connectivity you can
use shortcut method `observeSimple()`. This observable, instead of `Observable<RxNetworkInfo>`, 
will return `Observable<Boolean>` extracting information by simply mapping original `isConnected()` 
method. Take a look:

```java
rxNetwork.observeSimple()
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe(this::onConnectionInfo);
         
void onConnectionInfo(Boolean isConnected) {
    System.out.println("Network is " + (isConnected ? "connected" : "not connected")); 
}      
```

#### Observing with custom strategy

This one is just to give you best of both world. So, apart from setting up your default strategy with
RxNetwork initialization process described earlier, you could alter your observation needs *ad-hoc* 
by using your own custom strategy with `observe(@NonNull NetworkObservingStrategy strategy)` 

This may be useful if you decide, for example, that you need slightly different configuration 
in some part of your application but don't want to inject another RxNetwork instance. You can 
do that easily:
 
```java
rxNetwork.observe(new YourCustomNetworkObservingStrategy())
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe(...);
``` 

#### Observing with NetworkCapabilities

Starting from *Lollipop* (`API 21+`) there is new mechanism in Android for observing network changes 
based on new [NetworkCallback](#https://developer.android.com/reference/android/net/ConnectivityManager.NetworkCallback.html).
There is also a new [NetworkCapabilities](#https://developer.android.com/reference/android/net/NetworkCapabilities.html)
class that represents the capabilities of a network and [NetworkRequest](#https://developer.android.com/reference/android/net/NetworkRequest.html)
that is used to request a network via [`ConnectivityManager#registerNetworkCallback`](#https://developer.android.com/reference/android/net/ConnectivityManager.html#registerNetworkCallback(android.net.NetworkRequest,%20android.net.ConnectivityManager.NetworkCallback))

**RxNetwork takes advantage of this new mechanism when trying to observe network changes**. Under 
the hood it uses default `NetworkRequest` which in turn uses default `NetworkCapabilities` to 
observe "wanted" network. As always you can filter what kind of network you want to observe on 
(see [NetworkCapabilities related filtering](#networkcapabilities-related-filtering) section). If 
you'll be relying on library's default this would mostly make sense in regard to `transportType` 
filter but if you want to dive in more when deciding more about the network you want to observer 
you can provide your own `NetworkRequest` instance to RxNetwork, for example:

```java
NetworkRequest request = new NetworkRequest.Builder()
                .addTransportType(TRANSPORT_WIFI)
                .addTransportType(TRANSPORT_CELLULAR)
                .addCapability(NET_CAPABILITY_INTERNET)
                .addCapability(NET_CAPABILITY_NOT_VPN)
                .build();

RxNetwork rxNetwork = RxNetwork.builder().defaultNetworkRequest(request).init(context);

// proceed with observing as you normally would
```

As you can see Android's `NetworkRequest.Builder` lets you configure multiple transport types and
similarily multiple capabilities, but **please read official documentation to know exactly how this
all works** and how those options differ from each other.

#### Filtering

With RxNetwork if you're only interested in particular kind of network information, 
**you can** use built-in filters to **react on a concrete state, states, type or types changes**. 
This can be done with standard RxJava `filter(...)` method and may look something like this:

```java
import static android.net.ConnectivityManager.TYPE_MOBILE;
import static android.net.ConnectivityManager.TYPE_WIFI;
import static android.net.NetworkInfo.State.CONNECTED;
import static android.net.NetworkInfo.State.DISCONNECTED;

import static greyfox.rxnetwork.internal.strategy.network.predicate.RxNetworkInfoPredicate.Type.hasType;
import static greyfox.rxnetwork.internal.strategy.network.predicate.RxNetworkInfoPredicate.Type.hasState;

(...)

rxNetwork.observe()
    .observeOn(AndroidSchedulers.mainThread())
    .filter(hasType(TYPE_WIFI, TYPE_MOBILE))
    .filter(hasState(CONNECTED, DISCONNECTED))
    .subscribe(...)
```

For two types that would probably be mostly used: *wifi* and *mobile* we also provided handy 
shortcuts:

```java
import static greyfox.rxnetwork.internal.strategy.network.predicate.RxNetworkInfoPredicate.Type.IS_MOBILE;
import static greyfox.rxnetwork.internal.strategy.network.predicate.RxNetworkInfoPredicate.Type.IS_WIFI;

(...)

rxNetwork.observe()
    .observeOn(AndroidSchedulers.mainThread())
    .filter(IS_WIFI) // or filter(IS_MOBILE)
    .subscribe(...)
```

Same goes for the two most interested states:

```java
import static greyfox.rxnetwork.internal.strategy.network.predicate.RxNetworkInfoPredicate.State.IS_CONNECTED;
import static greyfox.rxnetwork.internal.strategy.network.predicate.RxNetworkInfoPredicate.State.IS_DISCONNECTED;

(...)

rxNetwork.observe()
    .observeOn(AndroidSchedulers.mainThread())
    .filter(IS_CONNECTED) // or filter(IS_DISCONNECTED)
    .subscribe(...)
```

**NetworkCapabilities related filtering**

There is new type of filter available for you when observing network on Android `API 21+`, namely 
`hasTransport`. This one is related to new `NetworkCapabilities`. Usage is similar to what you 
already know:

```java
import static android.net.NetworkCapabilities.TRANSPORT_CELLULAR;
import static android.net.NetworkCapabilities.TRANSPORT_WIFI;

import static greyfox.rxnetwork.internal.strategy.network.predicate.RxNetworkInfoPredicate.Capabilities.hasTransportType;

(...)

rxNetwork.observe()
    .observeOn(AndroidSchedulers.mainThread())
    .filter(hasTransportType(TRANSPORT_WIFI, TRANSPORT_CELLULAR))
    .subscribe(...)
```

As you see multiple transports may be applied when searching for a network to satisfy a request. In 
the example above this would cause either a Wi-Fi network or an Cellular network to be selected. 
This is logically different than `NetworkCapabilities.NET_CAPABILITY_*` (which is why it doesn't 
make much sense for the library to filter by concrete capabilities). If you're in doubt what we mean
by that should definitely check out Android's source code for `NetworkCapabilities` class to see how 
capabilities and transport types work.

There also two additional bandwidth-related predicates: `isSatisfiedByUpBandwidth` and `isSatisfiedByDownBandwidth` 

```java

import static greyfox.rxnetwork.internal.strategy.network.predicate.RxNetworkInfoPredicate.Capabilities.isSatisfiedByUpBandwidth;
import static greyfox.rxnetwork.internal.strategy.network.predicate.RxNetworkInfoPredicate.Capabilities.isSatisfiedByDownBandwidth;

(...)

private static final int UPSTREAM_BANDWIDTH = 1024 // in Kbps
private static final int DOWNSTREAM_BANDWIDTH = 2048 // in Kbps

rxNetwork.observe()
    .observeOn(AndroidSchedulers.mainThread())
    .filter(isSatisfiedByUpBandwidth(UPSTREAM_BANDWIDTH)    
    .filter(isSatisfiedByDownBandwidth(DOWNSTREAM_BANDWIDTH))
    .subscribe(...)
```

**Please, note:** bandwidth is never measured, but rather is inferred from technology type and 
other link parameters. It is also by default in `Kbps` vis-à-vis original `getLinkUpstreamBandwidthKbps` 
and `getLinkUpstreamBandwidthKbps` methods. Any calculations are up to you. When in doubt please 
refer to [`NetworkCapabilities`](https://developer.android.com/reference/android/net/NetworkCapabilities.html) 
class in Android API to get a grip.

### Observing true internet access

Although observing network connection gives you good approximation of what is going on with your 
network, you should bear in mind it doesn't actually guarantee there is a real working internet 
access. Of course Android `NetworkInfo#isConnected()` method tries to handle cases like flaky mobile 
networks, airplane mode, and restricted background data but ultimately it doesn't necessarily mean 
there is actual network traffic going on. Standard solution using `ConnectivityManager` described 
in [Android Dev](https://developer.android.com/training/monitoring-device-state/connectivity-monitoring.html) 
**does not really check for real internet access, it just checks if a connection is established** 

To put it in simple terms: **having a network connection doesn't mean you have true internet access**. 
Obvious examples can be when user is connected to a coffee shop's WiFi portal but can't get to 
the internet. In such case connection to a local wifi hotspot would've return true, even if hotspot 
would not allow you to go through to the internet. Similar situations might occur with VPN-related 
setups, connections to router that has no outside internet access, or with something banal and 
prosaic like server hang-on. 

If you are interested in tackling those situations and **if you want to be absolutely sure 
that your observables provide you information about internet accessibility** please read on. 

#### Observing internet access

As you might've suspected: you can observe real internet connectivity with RxNetwork. This is done 
simply by using `observeInternetAccess` method. This observable will simply return `true` if there 
is real Internet connection and `false` if not.

```java
rxNetwork.observeInternetAccess()
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe(connectedToInternet -> System.out.println("You are: " 
        + (connectedToInternet ? "connected" : "not connected")));
``` 

Of course similar as before you can use your own strategy. All you need to do is to simply implement
`InternetObservingStrategy` interface and pass it like here:

```java
rxNetwork.observeInternetAccess(new YourCustomInternetObservingStrategy())
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe(...);
``` 

So, let's say for example, you want to use socket-based approach (maybe you want it to check your 
own server or whatever). You can either do with your own strategy, as described above, or simply use 
[`SocketInternetObservingStrategy`](https://github.com/greyfoxit/RxNetwork/blob/master/rxnetwork/src/main/java/greyfox/rxnetwork/internal/strategy/internet/impl/SocketInternetObservingStrategy.java) 
that is already provided for you in the library:

```java
rxNetwork.observeInternetAccess(SocketInternetObservingStrategy.create())
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe(...);
```

Please check out the code if you're wondering about the defaults. If you're not happy with them, as 
always you can configure the details to your own needs, for example:

```java
InternetObservingStrategy internetObservingStrategy = SocketInternetObservingStrategy.builder()
    .endpoint("www.apple.com").port(80).delay(1500).interval(5000).timeout(5000)
    .build();

rxNetwork.observeInternetAccess(internetObservingStrategy)
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe(...);
```

The same goes for [`HttpOkInternetObservingStrategy`](https://github.com/greyfoxit/RxNetwork/blob/master/rxnetwork/src/main/java/greyfox/rxnetwork/internal/strategy/internet/impl/HttpOkInternetObservingStrategy.java) 
and even [`WalledGardenInternetObservingStrategy`](https://github.com/greyfoxit/RxNetwork/blob/master/rxnetwork/src/main/java/greyfox/rxnetwork/internal/strategy/internet/impl/WalledGardenInternetObservingStrategy.java)
that is used as the library's default under the hood.

#### Built-in internet observing strategies

There are three, fully configurable, internet observing strategies that you can use:

- first is the one used by the library under the hood and partially mentioned in the 
[Introduction](#introduction), namely [`WalledGardenInternetObservingStrategy`](https://github.com/greyfoxit/RxNetwork/blob/master/rxnetwork/src/main/java/greyfox/rxnetwork/internal/strategy/internet/impl/WalledGardenInternetObservingStrategy.java) 
It does its bit by checking special endpoint configured for returning `HTTP_NO_CONTENT` 
(Status-Code 204). 

    **Under the hood it uses Android team's own approach** (as seen in `android.net.wifi.WifiWatchdogStateMachine`) 
    but with more sensible endpoint, hence taking care of Great China Wall blocking all that is Google 
    (or *all that is evil* up there) At the time of writing this endpoint is: 
    [http://google.cn/generate_204](http://google.cn/generate_204) and it's supposed to be not 
    blocked, but of course you are free to configure your own endpoint.
    
- next provided strategy is the [`HttpOkInternetObservingStrategy`](https://github.com/greyfoxit/RxNetwork/blob/master/rxnetwork/src/main/java/greyfox/rxnetwork/internal/strategy/internet/impl/HttpOkInternetObservingStrategy.java) 
It's a variation on the previous theme that checks for `HTTP_OK` (Status-Code 200). By default it 
uses [http://www.google.cn/blank.html](http://www.google.cn/blank.html) address mostly because it 
seems to be not blocked in China yet and because of it's zero-length response body (saving bandwidth) 
As always you can use the address of your own choice that conform to this mechanism. For example 
Apple seems to have similar one: [http://captive.apple.com](http://captive.apple.com) but it can be 
any other that works
 
- last one is the self-explanatory [`SocketInternetObservingStrategy`](https://github.com/greyfoxit/RxNetwork/blob/master/rxnetwork/src/main/java/greyfox/rxnetwork/internal/strategy/internet/impl/SocketInternetObservingStrategy.java) 
that tries to connect to the given endpoint via socket-based mechanism. Example usage is shown 
already in [Observing real internet access](#observing-real-internet-access) section

## Examples

Too see exemplary application (with DI, Retrolambda and all) **check out** `app` directory 
in the repo.

## Tests

Tests are available in `rxnetwork/src/test/java/` directory and can be executed on JVM without any 
emulator or Android device from Android Studio or CLI with the following command:

```
./gradlew test
```

Or if you want altogether to execute tests and generate coverage reports you can simply run the 
following:

```
./gradlew test jacocoTestReport
```

## Code style

This project uses slightly modified `SquareAndroid` code style. You can grab it from [here](https://github.com/greyfoxit/java-code-styles)
if you decide to contribute.

## Contributing
    
Please [see the guidelines for contributing](CONTRIBUTING.md) before creating pull requests.

## Changelog

This project adheres to [Semantic Versioning](http://semver.org/). All notable changes are 
documented in [CHANGELOG](CHANGELOG.md) file.

## Credits

[@pwittchen](https://github.com/pwittchen) for the starting idea and original [ReactiveNetwork](https://github.com/pwittchen/ReactiveNetwork) library

## License

    Copyright 2017 Greyfox, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

<!-- references -->

[circleci-shield]: https://circleci.com/gh/radekkozak/RxNetwork/tree/master.svg?style=shield&circle-token=8e8a3277443ce3e39756d5d9167522837a947d58
[circleci-link]: https://circleci.com/gh/radekkozak/RxNetwork/tree/master
[codecov-shield]: https://codecov.io/gh/radekkozak/RxNetwork/branch/master/graph/badge.svg?token=dPBP8eA8CJ
[codecov-link]: https://codecov.io/gh/radekkozak/RxNetwork
[maven-shield]: https://img.shields.io/maven-central/v/it.greyfox.rxnetwork/rxnetwork.svg
[maven-link]: http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22it.greyfox.rxnetwork%22%20AND%20a%3A%22rxnetwork%22
[license-shield]: https://img.shields.io/badge/License-Apache%202.0-blue.svg
[license-link]: https://opensource.org/licenses/Apache-2.0
