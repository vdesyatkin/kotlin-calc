# kotlin-calc

Just-for-fun calculator with lots of abilities.  
For example, it can do this kind of stuff:  
(finding the m-th prime number, but not bigger than n)  

```
@n = args.get(0); @m = args.get(1);
if (@m <= 1, return 2);
@arr = array(floor(@n/2)); @number = 2;
range(3, @n, 2).foreach(@current -> {
	if (@arr.get(floor(@current / 2) - 1) == 0, {
		if (@number++ == @m, return @current)
	});
	range(@current ^ 2, @n, @current).foreach(@next -> {
		if (@next % 2 == 1, @arr.set(floor(@next / 2) - 1, 1))
	})
});
return 0
```

or almost the same with a statefull class (finding a sequence of prime numbers)

```
@erato = class(@max).ctor {
	this.@current = 2;
	this.@arr = array(floor(this.@max/2))
};
@erato.@next.method {
	if (this.@current == 2, { this.@current = 3; return 2 });
    range(this.@current ^ 2, this.@max, this.@current).foreach(@i -> {
		if (@i % 2 == 1, this.@arr.set(floor(@i / 2) - 1, 1))
	});
	@prime = if (this.@arr.get(floor(this.@current / 2) - 1) == 0, this.@current, null);
	this.@current += 2;
	return @prime
};
@erato.@primes.method {
	while.map(this.@current < this.@max, ::(this.@next)).filter(it != null)
};
@erato.new(args.get(0)).@primes.call().skip(args.get(1)).take(args.get(2))
```
