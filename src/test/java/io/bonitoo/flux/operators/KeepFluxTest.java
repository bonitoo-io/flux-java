package io.bonitoo.flux.operators;

import java.util.ArrayList;
import java.util.Collection;

import io.bonitoo.flux.Flux;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (02/08/2018 11:43)
 */
@RunWith(JUnitPlatform.class)
class KeepFluxTest {

    @Test
    void keepByArray() {

        Flux flux = Flux
                .from("telegraf")
                .keep(new String[]{"host", "_measurement"});

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> keep(columns: [\"host\", \"_measurement\"])");
    }

    @Test
    void keepByCollectionArray() {

        Collection<String> columns = new ArrayList<>();
        columns.add("host");
        columns.add("_value");

        Flux flux = Flux
                .from("telegraf")
                .keep(columns);

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> keep(columns: [\"host\", \"_value\"])");
    }

    @Test
    void keepByFunction() {

        Flux flux = Flux
                .from("telegraf")
                .keep("col =~ /usage*/");

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> keep(fn: (col) => col =~ /usage*/)");
    }

    @Test
    void keepByParameters() {

        Flux flux = Flux
                .from("telegraf")
                .keep()
                .withFunction("col =~ /inodes*/");

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> keep(fn: (col) => col =~ /inodes*/)");
    }
}