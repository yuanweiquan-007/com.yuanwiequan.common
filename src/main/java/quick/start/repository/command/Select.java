package quick.start.repository.command;

import quick.start.Builder;
import quick.start.entity.Entity;

public abstract class Select<E extends Entity> extends CommandForEntity<E> implements Builder {

}