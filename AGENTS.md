# Contribution guidelines for Coding agents

## Dev environment tips
- Use existing Gradle wrapper to save time unless you need to introduce a change to Gradle configuration or version.

## Testing instructions
- Always run the whole unit test suite using Gradle.
- When creating new unit tests, use JUnit5, Mockito for mocking and assertJ for assertions. Use parameterised tests with a method source for repetitive scenarios.

## Code style and conventions
- Use latest available language features.
- Use lambdas, Stream API and functional style where possible.
- Use var keyword for declaring local variables.
- Respect the Single responsibility principle, avoid having bloated "god" classes.
- Use Google Java format.
- Use lombok annotations. Use @AllArgsConstructor unless there is a good reason to use @ReqArgsConstructor.
- Don't overdo the code documentaion - comment only on class/method level, not on individual code lines unless they contain very peculiar or easy to overlook side effects.

## PR instructions
- When possible, break your work down into separate compartmentalised PRs.
- Always run unit tests before committing.
- Title format: [<Affected service>] <Title>.