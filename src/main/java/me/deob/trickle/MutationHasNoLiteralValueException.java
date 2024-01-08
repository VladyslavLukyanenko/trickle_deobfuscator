package me.deob.trickle;

public class MutationHasNoLiteralValueException extends Exception {
  private final VarMutation mutation;

  public MutationHasNoLiteralValueException(VarMutation mutation, String message) {
    super(message);
    this.mutation = mutation;
  }

  public VarMutation getMutation() {
    return mutation;
  }
}
