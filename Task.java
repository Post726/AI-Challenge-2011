
public class Task {
	public enum Type
	{
		ATTACK_HILL,
		GATHER,
		GUARD
	}
	
	public Task(Type type, Route route)
	{
		this.type = type;
		this.route = route;
	}
	
	public Type type;
	public Route route;
}

