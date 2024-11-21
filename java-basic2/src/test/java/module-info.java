module example {
    requires org.slf4j;
    requires aws.lambda.java.core;
    requires org.junit.jupiter.api;
    requires jackson.databind;
    requires jackson.core;

    exports example;
}