package ar.edu.utn.frba.dds;

import spark.Spark;

public class Main {
  public static void main(String[] args) {
    Spark.port(9000);
    Spark.get("/", ((request, response) -> "Hello world!"));
  }
}
