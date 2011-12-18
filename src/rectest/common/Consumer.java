package rectest.common;

public interface Consumer<IN, OUT> {

    OUT consume(IN in);
}
