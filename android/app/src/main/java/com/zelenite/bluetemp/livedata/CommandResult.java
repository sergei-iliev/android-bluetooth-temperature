package com.zelenite.bluetemp.livedata;

public class CommandResult<T> {
      private T result;
      private Exception error;

    public T getResult() {
        return result;
    }

    public Exception getError() {
        return error;
    }

    public CommandResult(T result) {
        this.result = result;
    }

    public CommandResult(Exception error) {
        this.error = error;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public void setError(Exception error) {
        this.error = error;
    }
}
