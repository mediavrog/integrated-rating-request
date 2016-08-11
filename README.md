# Integrated Rating Request [ ![Download](https://api.bintray.com/packages/mediavrog/maven/integrated-rating-request/images/download.svg) ](https://bintray.com/mediavrog/maven/integrated-rating-request/_latestVersion)
<sup>Badges galore</sup>
[![Gitter](https://badges.gitter.im/mediavrog/integrated-rating-request.svg)](https://gitter.im/mediavrog/integrated-rating-request?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=body_badge)
[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-Integrated%20Rating%20Request-brightgreen.svg?style=flat)](http://android-arsenal.com/details/1/3283)
[![Platform](https://img.shields.io/badge/platform-android-green.svg)](http://developer.android.com/index.html)
[![API](https://img.shields.io/badge/API-11%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=11)
[![License](https://img.shields.io/badge/License-MIT-blue.svg?style=flat)](http://opensource.org/licenses/MIT)

#### A better way to ask for ratings.

<img src="https://github.com/mediavrog/integrated-rating-request/blob/master/art/flow.png?raw=true" width="500">

As [popularized by Circa](https://medium.com/circa/the-right-way-to-ask-users-to-review-your-app-9a32fd604fca).

* get better ratings by showing the rate reminder in your layout instead of popup
* full control over design, irr manages only the flow and visibility of the rate reminder
* customize the conditions when to show the rate reminder
* .. or supply your own RuleEngine altogether for full control (backed by [ruli](https://github.com/mediavrog/ruli))
* adjustable callbacks for rating and feedback
* list adapter decorator can manage visibility of irr in a list automatically so you don't have to

## Demo application

<a href="https://play.google.com/store/apps/details?id=net.mediavrog.samples.irr" target="_blank">
  <img alt="Get it on Google Play"
      src="https://play.google.com/intl/en_us/badges/images/generic/en-play-badge.png" height="60"/>
</a>

## Getting started

#### Gradle Dependency (jcenter)

Easily reference the library in your Android projects using this dependency in your module's `build.gradle` file.

```java
dependencies {
    compile 'net.mediavrog:integrated-rating-request:1.1.2'
}
```

## Usage

* **Track app starts** in your custom application `Application#onStart` or similar place using `DefaultRuleEngine.trackAppStart(Context ctx);`. This is required for the default rule engine to work properly. The default rule engine is backed by `SharedPreferences` and writes data to `APP_PACKAGE_NAME.irr_default_rule_engine`.
* **Include the layout** `net.mediavrog.irr.IrrLayout` in your layout. Add the namespace `xmlns:irr="http://schemas.android.com/apk/res-auto"` to the root element.
* **Add three child layouts** for the user flow identified by
	* `@id/irr_nudge_layout` Ask user if he likes the app.
	* `@id/irr_rate_layout` If so nudge user to rate app.
	* `@id/irr_feedback_layout` Else ask for feedback.
* **Each must have two buttons** for user interaction identified by
	* `@id/irr_nudge_accept_btn` User enjoys the app. Proceed to rating.
	* `@id/irr_nudge_decline_btn` User doesn't enjoy the app. Proceed to feedback.
	* `@id/irr_rate_accept_btn` User wants to leave a rating.
	* `@id/irr_rate_decline_btn` User declines leaving a rating.
	* `@id/irr_feedback_accept_btn` User wants to leave feeback.
	* `@id/irr_feedback_decline_btn` User declines to leave feedback.
* **Set user action urls**
	* `irr:ratingUrl` sets the url to `ACTION_VIEW` once a user wants to rate the app. _(Default: `https://play.google.com/store/apps/details?id=APP_PACKAGE_NAME`)_
	* `irr:feedbackUrl` sets the url to `ACTION_VIEW` once a user wants to give feedback. _(Default: null)_
* [optional] **Tune the rule engine** parameters:
	* `irr:defaultRuleAppStartCount` Minimum amount of app starts before nudging for first time. _(Default: 10)_
	* `irr:defaultRuleDistinctDays` Minimum amount of distinct days the user has opened the app before nudging for first time.  _(Default: 3)_
	* `irr:defaultRuleDismissPostponeDays` Postpone nudging the user again by some days when he gave feedback or left the flow without action.  _(Default: 7)_
	* `irr:defaultRuleDismissMaxCount` Maximum number of times a user can dismiss before nudging him is deactivated forever.  _(Default: 3)_

This should cover a basic setup. See the demo code for more details.

Example `Basic intgrated rating request usage` _(styles are taken from the demo app)_

```xml
<net.mediavrog.irr.IrrLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:irr="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    irr:defaultRuleAppStartCount="10"
    irr:defaultRuleDismissMaxCount="3"
    irr:defaultRuleDismissPostponeDays="7"
    irr:defaultRuleDistinctDays="3"
    irr:feedbackUrl="@string/link_feedback"
    irr:ratingUrl="@string/link_store">

    <LinearLayout
        android:id="@id/irr_nudge_layout"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical">

        <TextView
            android:id="@id/irr_nudge_text"
            style="@style/Irr.Default.Text"
            android:text="Enjoying this app?"/>

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal">

            <Button
                android:id="@id/irr_nudge_decline_btn"
                style="@style/Irr.Default.Button"
                android:text="Not really"/>

            <Button
                android:id="@id/irr_nudge_accept_btn"
                style="@style/Irr.Default.Button.Accept"
                android:text="Yes!"/>
        </LinearLayout>

    </LinearLayout>

    <LinearLayout
        android:id="@id/irr_rate_layout"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical">

        <TextView
            android:id="@id/irr_rate_text"
            style="@style/Irr.Default.Text"
            android:text="How about a rating in the app store then?"/>

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal">

            <Button
                android:id="@id/irr_rate_decline_btn"
                style="@style/Irr.Default.Button"
                android:text="No, thanks"/>

            <Button
                android:id="@id/irr_rate_accept_btn"
                style="@style/Irr.Default.Button.Accept"
                android:text="Ok, sure."/>
        </LinearLayout>
    </LinearLayout>

    <LinearLayout
        android:id="@id/irr_feedback_layout"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical">

        <TextView
            android:id="@id/irr_feedback_text"
            style="@style/Irr.Default.Text"
            android:text="Would you mind giving us some feedback?"/>

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal">

            <Button
                android:id="@id/irr_feedback_decline_btn"
                style="@style/Irr.Default.Button"
                android:text="No, thanks"/>

            <Button
                android:id="@id/irr_feedback_accept_btn"
                style="@style/Irr.Default.Button.Accept"
                android:text="Ok, sure."/>
        </LinearLayout>
    </LinearLayout>

</net.mediavrog.irr.IrrLayout>
```

## Advanced usage

### Auto-management in list views

> Goal: Show IRRLayout in list views
 
IRR can handle the injection of the IRRLayout in any list adapter derived from `BaseAdapter`. Just pass your adapter decorated with `IrrAdapterDecorator` to your list view.

Example `Inject IRRLayout at 10th position`

```java
ListView lv = (ListView) findViewById(android.R.id.list);
ArrayAdapter myAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1, values);

// show the irr view at the 10th position (index starts at 0)
// optional: pass a custom rule engine as 5th parameter
decoratedAdapter = new IrrAdapterDecorator(this, myAdapter, 9, R.layout.standard_irr_layout);
lv.setAdapter(decoratedAdapter);
```
If you modify data for rule engine yourself, notify the adapter to refresh using `decoratedAdapter.notifyRuleEngineStateChanged();` 

### Custom action handler

> Goal: Customize the behaviour for giving feedback or rating the app

Implement `IrrLayout.OnUserActionListener` and supply it to the IRRLayout `irr.setOnUserActionListener(myOnUserActionListener)`. 

If you only want to overwrite either action handling, extend `DefaultOnUserActionListener` instead.

### Custom rule engine

> Goal: Roll your own customized logic for showing the IRRLayout

* First, set the IRRLayout attribute `irr:useCustomRuleEngine` to true. Note that all `irr:defaultRule*` attributes will not have any effect any more. 
* Create your own set of rules and feed them to a `RuleEngine`. Check the documentation of [ruli](https://github.com/mediavrog/ruli) for more details.
* Finally, supply your rule engine to the IRRLayout. 

Example `Nudge user on weekends`

```java
// create weekend rule
RuleSet evenDayRule = new RuleSet.Builder()
        .addRule(new Rule() {
            @Override
            public boolean evaluate() {
                int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
                return dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY;
            }

            @Override
            public String toString(boolean evaluate) {
                return "Is today Saturday or Sunday?" + (evaluate ? " => " + this.evaluate() : "");
            }
        }).build();

// .. put into a custom rule engine
ArrayList<Rule> rules = new ArrayList<>();
rules.add(evenDayRule);
engine = new RuleEngine(rules);

// evaluate the rule engine any time
engine.evaluate();

// supply to the IRRLayout
irr = (IrrLayout) findViewById(R.id.irr_layout);
irr.setRuleEngine(engine); // layout attr irr:useCustomRuleEngine must be set to true!
```

## Contributers

<!--* Dec 22, 13 - Vladimir Mironov ([@MironovNsk](https://github.com/nsk-mironov)) - Custom Expanded Panel Height-->

If you have an awesome pull request, send it over!

## Changelog

* 1.1.2
  - Removed non-library resources
  - fix for onDismiss/onAccept events in Nudge state

* 1.1.1 
  - Fixed bug with dicustom view types in IrrAdapterDecorator
  
* 1.1.0
  - Initial Release backed by ruli 1.1.0 

## Known users

| App | Description | Integration |
| --- | --- | --- |
| [Suikakeibo](https://play.google.com/store/apps/details?id=net.mediavrog.ic_card_expensetracker) | Suica reader/ledger - track your expenses with a simple touch | <a href="https://github.com/mediavrog/integrated-rating-request/blob/master/art/app-suikakeibo.png?raw=true" target="_blank"><img src="https://github.com/mediavrog/integrated-rating-request/blob/master/art/app-suikakeibo.png?raw=true" width="100"></a>

If you are using the library and you would like to have your app listed, simply send a pull request.

## License

```
The MIT License (MIT)

Copyright (c) 2016 mediavrog

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
```
