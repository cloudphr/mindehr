## MindEHR

![Build Status](https://github.com/cloudphr/mindehr/workflows/Build/badge.svg)

**`MindEHR`** is a web application for OpenEHR Archetype and Template generation.
With this service, you can convert the mindmap based model to OpenEHR archetypes and templates automatically.

## Usage

### Prerequisites:

-   Install [Java 17](https://openjdk.java.net/projects/jdk/17/)

### Get and Run the `jar` file

-   Download the release from [MinEHR released package](https://github.com/orgs/cloudphr/packages?repo_name=mindehr).
-   Run it on your server:
    ```shell
    java -jar mindehr-x.y.z-*********.jar
    ```

### Using the MindEHR service

-   Prepare your mindmap based model file with the rules of [Mindmap Specification of MindEHR](./docs/Specification.md) and you may find a simple example [here](./docs/examples/small.xmind).
-   Open your browser and enter http://{_your-ip_}:8080, you can convert your xmind file to the OpenEHR archetypes and templates automatically.
-   Enjoy it!

## Development

It is developed with [Java 17](https://docs.oracle.com/en/java/javase/17/) and built with [Gradle](https://docs.gradle.org/7.6.1/userguide/userguide.html).

We recommend using the `gradlew` script in the project to build it. The gradle wrapper version is specified in [gradle-wrapper.properties](https://github.com/cloudphr/mindehr/blob/master/gradle/wrapper/gradle-wrapper.properties).
If you would prefer to build with the local gradle package, we recommend it with the 7.6.1 version, although other gradle versions should work.

### Prerequisites:

-   Install [Java 17](https://openjdk.java.net/projects/jdk/17/)

### Clone this project:

```shell
git clone https://github.com/cloudphr/mindehr.git
```

### Install the dependencies

-   Install the dependencies:
    ```shell
    ./gradlew clean dependencies
    ```

### Develop new features

-   The [github flow](https://guides.github.com/introduction/flow/) is recommended during the development.
-   Please write test cases for your new code.

### Test your added features

```shell
./gradlew clean test
```

### Boot your customized application

```shell
./gradlew bootRun
```

### Using your customized MindEHR service

-   Open your browser and enter http://{_your-ip_}:8080, you can convert your xmind file to the OpenEHR archetypes and templates automatically.
-   Enjoy it!

## Contributing

Hi there! We're thrilled that you'd like to contribute to this project. Your help is essential for keeping it great.

Contributions to this project are released to the public under the [project's open-source license](./LICENSE).

### Submitting a Pull Request

1. [Fork](https://github.com/cloudphr/mindehr/fork) and clone the repository
1. Configure and install the dependencies: `./gradlew build`
1. Create a new branch: `git checkout -b my-branch-name`
1. Make your change and check your work using `./gradlew clean check`
1. Push to your fork and [submit a pull request](https://github.com/cloudphr/mindehr/compare)
1. Pat your self on the back and wait for your pull request to be reviewed and merged.

Here are a few things you can do that will increase the likelihood of your pull request being accepted:

-   Follow the coding style used in this project. You can do this by running `./gradlew checkstyle`.
-   Keep your change as focused as possible. If there are multiple changes you would like to make that are not dependent upon each other, consider submitting them as separate pull requests.
-   Write [good commit messages](http://tbaggery.com/2008/04/19/a-note-about-git-commit-messages.html).

### Resources

-   [How to Contribute to Open Source](https://opensource.guide/how-to-contribute/)
-   [Using Pull Requests](https://help.github.com/articles/about-pull-requests/)
-   [GitHub Help](https://help.github.com)
