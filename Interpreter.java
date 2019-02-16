package whilelang.interp;
import java.util.*;
import whilelang.ast.*;

public class Interpreter implements Visitor<Integer>{
	public HashMap<String, Integer> variables = new HashMap<String, Integer>();

	public Integer visit(Print n) {
		// TODO Implement this!
		String expression=n.msg+Integer.toString(variables.get(n.varID));
		System.out.println(expression);
		return null;
	}

	public Integer visit(Assign n) {
		// TODO Implement this!
		variables.put(n.varID,n.expr.accept(this));
		return n.expr.accept(this);
	}

	public Integer visit(Skip n) {
		// TODO Implement this!
		return null;
	}

	public Integer visit(Block n) {
		// TODO Implement this!
		for( Statement stat: n.body)
			stat.accept(this);
		return null;
	}

	public Integer visit(IfThenElse n) {
		// TODO Implement this!
		if((n.expr.accept(this)==1))
			return n.then.accept(this);
		else
			return n.elze.accept(this);
	}

	public Integer visit(While n) {
		// TODO Implement this!
		while(n.expr.accept(this)!=0)
			n.body.accept(this);
		return null;
	}

	public Integer visit(For n) {
		// TODO Implement this!
		n.init.accept(this);
		while(n.expr.accept(this)!=0){
			n.body.accept(this);
			n.step.accept(this);
		}
		return null;
	}


	public Integer visit(Var n) {
		// TODO Implement this!
        if(variables.get(n.varID) != null)
        {
            return variables.get(n.varID);
        }
        variables.put(n.varID,0);
        return 0;
	}

	public Integer visit(IntLiteral n) {
		// TODO Implement this!
		return n.value;
	}

	public Integer visit(Plus n) {
		// TODO Implement this!
		return n.lhs.accept(this)+n.rhs.accept(this);
	}

	public Integer visit(Minus n) {
		// TODO Implement this!
		return n.lhs.accept(this)-n.rhs.accept(this);
	}

	public Integer visit(Times n) {
		// TODO Implement this!
		return n.lhs.accept(this)*n.rhs.accept(this);
	}

	public Integer visit(Division n) {
		// TODO Implement this!
		return n.lhs.accept(this)/n.rhs.accept(this);
	}

	public Integer visit(Modulo n) {
		// TODO Implement this!
		return n.lhs.accept(this)%n.rhs.accept(this);
	}

	public Integer visit(Equals n) {
		// TODO Implement this!
		if(n.lhs.accept(this)==n.rhs.accept(this))
			return 1;
		else
			return 0;
	}

	public Integer visit(GreaterThan n) {
		// TODO Implement this!
		if(n.lhs.accept(this)>n.rhs.accept(this))
			return 1;
		else
			return 0;
	}

	public Integer visit(LessThan n) {
		// TODO Implement this!
		if(n.lhs.accept(this)<n.rhs.accept(this))
			return 1;
		else
			return 0;
	}

	public Integer visit(And n) {
		// TODO Implement this!
		if((n.lhs.accept(this)!=0) && (n.rhs.accept(this)!=0))
			return 1;
		else
			return 0;
	}

	public Integer visit(Or n) {
		// TODO Implement this!
		if((n.lhs.accept(this)!=0) || (n.rhs.accept(this)!=0))
			return 1;
		else
			return 0;
	}

	public Integer visit(Neg n) {
		// TODO Implement this!
		return -1*(n.expr.accept(this));
	}

	public Integer visit(Not n) {
		// TODO Implement this!
		if((n.expr.accept(this)==0))
			return 1;
		else
			return 0;
	}

	public Integer visit(UnaryExpression n) {
		// TODO Implement this!
		return n.accept(this);
	}

	public Integer visit(BinaryExpression n) {
		// TODO Implement this!
		return n.accept(this);
	}
}
