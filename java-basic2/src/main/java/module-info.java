module java.basic {
    requires jackson.databind;
    requires jackson.core;
    requires aws.lambda.java.core;
    requires org.apache.commons.codec;
    requires java.net.http;

    exports example;
}