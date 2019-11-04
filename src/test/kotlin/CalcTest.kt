import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class CalcTest {

    private val _parser: CalcParser = CalcParser()

    @Test
    fun numbers() {
        assertEquals(1.0 , calc("1"))
        assertEquals(1.0 , calc("01"))
        assertEquals(1.0 , calc("000001"))
        assertEquals(1.0 , calc("1.0"))
        assertEquals(1.0 , calc("1.0000"))
        assertEquals(1.0 , calc("1.0000"))
        assertEquals(1.1 , calc("1.1000"))
        assertEquals(11.0 , calc("1  1"))
        assertEquals(-1.0 , calc("-1"))
        assertEquals(-99999.9 , calc("-99999.9"))

        assertEquals(null , calc(""))
        assertEquals(null , calc(" "))
        assertEquals(null , calc("a"))
        assertEquals(null , calc(".0"))
        assertEquals(null , calc("0."))
        assertEquals(null , calc("1a"))
        assertEquals(null , calc("1,1"))
        assertEquals(null , calc("9" + Double.MAX_VALUE.toString()))
    }

    @Test
    fun precendenceAndAssociativity() {
        assertEquals(3.0 , calc("1 + 2"))
        assertEquals(1.0 , calc("2 - 1"))
        assertEquals(3.5 , calc("1.0 + 2.5"))
        assertEquals(2.0 , calc("1 + 2 + 3 - 4"))
        assertEquals(6.0 , calc("1 * 2 * 3"))
        assertEquals(7.0 , calc("1 + 2 * 3"))
        assertEquals(9.0 , calc("(1 + 2) * 3"))
        assertEquals(1.0 , calc("(1 + 2) / 3"))
        assertEquals(512.0 , calc("2 ^ 3 ^ 2"))
        assertEquals(64.0 , calc("(2 ^ 3) ^ 2"))

        assertEquals(6.0 , calc("(1,2) + 3"))
        assertEquals(10.0 , calc("(1,2) + (3,4)"))
        assertEquals(12.0 , calc("1 + (1,2) + (3,4) + 1"))
        assertEquals(10.0 , calc("1 + (2 .. 4)"))
        assertEquals(15.0 , calc("(1 .. 3) + (2 .. 4)"))
    }

    @Test
    fun brackets() {
        assertEquals(10.0 , calc("2 * (2 + 3)"))
        assertEquals(10.0 , calc("2 * [2 + 3]"))
        assertEquals(10.0 , calc("2 * {2 + 3}"))
        assertEquals(null , calc("2 * (2 + 3]"))
    }

    @Test
    fun comparison() {
        assertEquals(1.0 , calc("3>=3>=2"))
        assertEquals(1.0 , calc("3>=3>=2==2>1!=5<=8<9"))
        assertEquals(1.0 , calc("(1==1) is true"))
        assertEquals(1.0 , calc("(1==1) != false"))
        assertEquals(1.0 , calc("(1==1) is not false"))
        assertEquals(true , calcBoolean("2 + 2 == 4"))
    }

    @Test
    fun logical() {
        assertEquals(1.0 , calc("1 and 1"))
        assertEquals(0.0 , calc("1 and 0"))
        assertEquals(1.0 , calc("2 > 1 and 3 > 2"))
        assertEquals(0.0 , calc("2 > 1 and 3 < 2"))
        assertEquals(1.0 , calc("2 > 1 or 3 < 2"))
    }

    @Test
    fun functions() {
        assertEquals(1.0 , calc("sum(1)"))
        assertEquals(4.0 , calc("sum(1)+sum(1,2)"))
        assertEquals(6.0 , calc("sum(1,2,3)"))
        assertEquals(10.0 , calc("sum(range(1,4))"))
        assertEquals(10.0 , calc("(1,2,3,4).sum()"))
        assertEquals(10.0 , calc("[1..4].sum()"))
        assertEquals(4.0 , calc("[1..4].count()"))
        assertEquals(1.0 , calc("[1..4].min()"))
        assertEquals(4.0 , calc("[1..4].max()"))
        assertEquals(21.6 , calc("(2,2,2,2,100).avg()"))
        assertEquals(2.0 , calc("(2,2,2,2,100).med()"))
    }

    @Test
    fun variables() {

        assertEquals(2.0 , calc("@a = 2; @a"))
        assertEquals(5.0 , calc("@a = 2; @b = 3; return (@a + @b)"))
        assertEquals(true , calcBoolean("@a = 2; @b = 2; @a == @b"))

        assertEquals(10.0 , calc("@arr = array(10, 1); @arr.length"))
        assertEquals(10.0 , calc("@arr = array(10, 1); length(@arr)"))
        assertEquals(10.0 , calc("@arr = array(10, 1); size(@arr)"))
        assertEquals(1.0 , calc("@arr = array(10, 1); get(@arr, 5)"))
        assertEquals(1.0 , calc("@arr = array(10, 1); @arr.get[5]"))
        assertEquals(1.0 , calc("@arr = array(10, 1); @arr.get(5)"))
        assertEquals(2.0 , calc("@arr = array(10, 1); set(@arr, 5, 2); get(@arr, 5)"))

        assertEquals(10.0 , calc("@arr = 0..9; @arr.length"))
        assertEquals(3.0 , calc("@arr = 0..9; @arr.get[3]"))
        assertEquals(4.0 , calc("@arr = [1,2,3,4]; @arr.length"))
        assertEquals(8.0 , calc("@arr = [5,6,7,8]; @arr.get[3]"))
    }

    @Test
    fun collections() {
        assertEquals(10.0 , calc("@list = list(0..9); @list.length"))
        assertEquals(0.0 , calc("@list = list(); @list.length"))
        assertEquals(3.0 , calc("@list = list(0,1,2,3,4); @list.get[3]"))

        assertEquals(3.0 , calc("list(0,1,2,3,4).get[3]"))
        assertEquals(5.0 , calc("count(list(0,1,2,3,4).values)"))
        assertEquals(4.0 , calc("@list = list(0,1,2,3,4); @list.set(3, 4); @list.get(3)"))
        assertEquals(listOf(1.0,2.0,3.0,4.0) , calcList("list(1,2,3).add(4).values()"))

        assertEquals(1.0 , calc("@map = hashmap; @map.set(5, 10); @map.length"))
        assertEquals(10.0 , calc("@map = hashmap(); @map.set(5, 10); @map.get(5)"))
        assertEquals(1.0 , calc("@map = hashmap(); @map.set(5, 10); count(@map.keys)"))
        assertEquals(1.0 , calc("@map = hashmap(); @map.set(5, 10); count(@map.values)"))
        assertEquals(30.0 , calc("@map = hashmap().set(5, 10).set(7, 20); @map.get(5) + @map.get(7)"))

        assertEquals(3.0 , calc("@list = list(10..20); @list.find(13)"))
        assertEquals(3.0 , calc("@list = list(1,2,3,2,4); @list.find(2,2)"))
        assertEquals(3.0 , calc("@list = list(1,2,3,2,4); @list.findFrom(2,2)"))

        assertEquals(3.0 , calc("@list = list(0..9); @list.slice(2,5).length"))
        assertEquals(3.0 , calc("@list = list(0..9); @list.slice(2,5).get(1)"))
        assertEquals(4.0 , calc("@list = list(0..9); @list.sliceFrom(3).get(1)"))
        assertEquals(1.0 , calc("@list = list(0..9); @list.sliceTo(7).get(1)"))
        assertEquals(20.0 , calc("(list(0..9) .+ list(10..19)).length"))
        assertEquals(10.0 , calc("(list(0..9) .+ list(10..19)).get(10)"))

        assertEquals(10.0 , calc("[0..9].toList().length"))
        assertEquals(6.0 , calc("[0..9].filter{ it > 5 }.toList().get(0)"))

        assertEquals(6.0 , calc("list(list(1,2,3),list(4,5,6),list(7,8,9)).get(1).get(2)"))
        assertEquals(6.0 , calc("list(list(1,2,3),list(4,5,6),list(7,8,9)).get(1,2)"))

        assertEquals(listOf(1.0, 2.0, 3.0) , calcList("list(1,2,3).values"))
        assertEquals(3.0, calc("seq(1,2,3,4).elementAt(2)"))

        assertEquals(listOf("(5->10)","(100->200)"), calcStringList("hashmap().set(5,10).set(100,200).entries.map((@key, @value) -> " +
                "string.format('(%d->%d)', @key, @value))"))
        assertEquals(listOf(1.0,2.0,3.0,2.0,3.0,4.0), calcList("hashmap().set(1,3).set(2,4).entries.flatmap((@key, @value) -> " +
                "@key..@value)"))

        assertEquals(listOf(1.0,2.0,3.0,4.0), calcList("seq(1,2,3,2,4,1).distinct()"))

        assertEquals(listOf(1.0,2.0,3.0,4.0), calcList("seq(4,2,1,3).sort()"))
        assertEquals(listOf(4.0,3.0,2.0,1.0), calcList("seq(4,2,1,3).sortDesc()"))
        assertEquals(listOf(2.0,2.0,1.0,1.0,3.0), calcList("seq(2,1,2,1,3).group().flatmap(it.values())"))
        assertEquals(listOf(2.0,1.0,3.0), calcList("seq(2,1,2,1,3).group().map(it.groupKey)"))
    }

    @Test
    fun zip() {
        assertEquals(listOf(3.0, 5.0, 7.0) , calcList("@l1 = list(1,2,3);" +
                "@l2 = list(2,3,4,5); @l1.zip.map(@l2, (@i1, @i2) -> @i1 + @i2)"))

        assertEquals(listOf(4.0, 6.0, 8.0) , calcList("@l1 = list(1,2,3);" +
                "@l2 = list(2,3,4,5); @l3 = list(1,1,1); " +
                "zip.map(@l1, @l2, @l3, (@i1, @i2, @i3) -> @i1 + @i2 + @i3)"))

        assertEquals(listOf(1.0, 2.0, 1.0, 2.0, 3.0, 1.0, 3.0, 4.0, 1.0) ,
                calcList("@l1 = list(1,2,3);" +
                        "@l2 = list(2,3,4,5); @l3 = list(1,1,1); " +
                        "zip.fmap(@l1, @l2, @l3, (@i1, @i2, @i3) -> seq(@i1,@i2,@i3))"))
    }

    @Test
    fun incrementDecrement() {
        assertEquals(3.0 , calc("@a = 1; inc(@a,2)"))
        assertEquals(3.0 , calc("@a = 1; inc(@a,2); @a"))
        assertEquals(2.0 , calc("@a = 1; inc(@a)"))
        assertEquals(2.0 , calc("@a = 1; inc(@a); @a"))
        assertEquals(3.0 , calc("@a = 1; @a += 2"))
        assertEquals(3.0 , calc("@a = 1; @a += 2; @a"))
        assertEquals(2.0 , calc("@a = 1; ++@a"))
        assertEquals(2.0 , calc("@a = 1; ++@a; @a"))

        assertEquals(0.0 , calc("@a = 2; dec(@a,2)"))
        assertEquals(0.0 , calc("@a = 2; dec(@a,2); @a"))
        assertEquals(1.0 , calc("@a = 2; dec(@a)"))
        assertEquals(1.0 , calc("@a = 2; dec(@a); @a"))
        assertEquals(0.0 , calc("@a = 2; @a -= 2"))
        assertEquals(0.0 , calc("@a = 2; @a -= 2; @a"))
        assertEquals(1.0 , calc("@a = 2; --@a"))
        assertEquals(1.0 , calc("@a = 2; --@a; @a"))
    }

    @Test
    fun incrementDecrementPost() {
        assertEquals(1.0 , calc("@a = 1; incpost(@a,2)"))

        assertEquals(3.0 , calc("@a = 1; incpost(@a,2); @a"))
        assertEquals(1.0 , calc("@a = 1; incpost(@a)"))
        assertEquals(2.0 , calc("@a = 1; incpost(@a); @a"))
        assertEquals(1.0 , calc("@a = 1; @a++"))
        assertEquals(2.0 , calc("@a = 1; @a++; @a"))


        assertEquals(2.0 , calc("@a = 2; decpost(@a,2)"))
        assertEquals(0.0 , calc("@a = 2; decpost(@a,2); @a"))
        assertEquals(2.0 , calc("@a = 2; decpost(@a)"))
        assertEquals(1.0 , calc("@a = 2; decpost(@a); @a"))
        assertEquals(2.0 , calc("@a = 2; @a--"))
        assertEquals(1.0 , calc("@a = 2; @a--; @a"))

        assertEquals(4.0 , calc("@a = 1; @a++ + ++@a"))
        assertEquals(3.0 , calc("@a = 1; @a++ + ++@a; @a"))

    }

    @Test
    fun lambdas() {
        assertEquals(15.0, calc("@a = 2; @b = 1; sum(range(1,3).map(@x -> @x * @a + @b))"))
        assertEquals(15.0, calc("@a = 2; @b = 1; sum(range(1,3).map(it * @a + @b))"))
        assertEquals(4.0, calc("@a = 2; @b = 1; sum(range(1,3).filter(@x -> @x % 2 == @b))"))
        assertEquals(10.0 , calc("@arr = array(10, 1); " +
                "[0..@arr.length - 1].map(@i -> @arr.get[@i]).fold(0, (@accum, @x) -> @accum + @x)"))
        assertEquals(10.0 , calc("@arr = array(10, 1); " +
                "@arr.values.fold(0, (@accum, @x) -> @accum + @x)"))
        assertEquals(3.0 , calc("@x = 1; @a = [1..2].map(@x -> @x).sum(); @a"))
        assertEquals(11.0 , calc("@a = [1..2].flatmap(@x -> @x .. 3).sum(); @a"))
        assertEquals(11.0 , calc("@a = [1..2].map(@x -> @x .. 3).flatmap(@y -> @y.values).sum(); @a"))
        assertEquals(11.0 , calc("@a = [1..2].map(@x -> list(@x .. 3)).flatmap(@y -> @y.values).sum(); @a"))
        assertEquals(listOf(2.0,4.0,6.0), calcList("(1,2,3).map { it * 2 }"))

        assertEquals(true, calcBoolean("seq(1,2,3).any { it > 2 }"))
        assertEquals(false, calcBoolean("seq(1,2,3).any { it > 3 }"))
        assertEquals(true, calcBoolean("seq(1,2,3).all { it >= 1 }"))
        assertEquals(false, calcBoolean("seq(1,2,3).all { it > 1 }"))

        assertEquals(listOf(3.0,4.0,5.0), calcList("seq(1,2,3).filter(it>2).union(seq(4,5))"))
        assertEquals(listOf(3.0,4.0), calcList("seq(1,2,3,4,5).skipWhile(it<3).takeWhile(it<5)"))
    }

    @Test
    fun functionsInCode() {

        assertEquals(6.0 , calc("@m2 = func(@x, {@x * 2}); @m2.call(3)"))
        assertEquals(6.0 , calc("@m = func(@x, @y, {@x * @y}); @m.call(2, 3)"))

        assertEquals(30.0 , calc(
                "@m2 = func(@x, {@x * 2}); " +
                        "@m6 = func(@x, {@m2.call(@x) * 3}); " +
                        "call(@m6, 5)"))

        assertEquals(12.0 , calc(
                "@m2 = func(@x, {@x * 2}); " +
                        "@a = [1..3].map(@m2.lambda); " +
                        "sum(@a.values)"))
        assertEquals(12.0 , calc(
                "@m2 = func(@x, {@x * 2}); " +
                        "@a = [1..3].map(::@m2); " +
                        "sum(@a.values)"))
        assertEquals(15.0 , calc(
                "@f = func(@x, if(@x <= 1, 1, @x + @f.call(@x - 1))); @f.call(5)"))
        assertEquals(6.0 , calc("@m = func(@x, @y, {@x * @y}); " +
                     "@m2 = @m.carry(@x, 2); @m2.call(3)"))

        assertEquals(3.0 , calc("@m = func(@x, @y, {return (@x * @y)}); @m.call(2, 3) / 2"))
    }

    @Test
    fun paging() {
        assertEquals(6.0, calc("[1..10].skip(5).first()"))
        assertEquals(15.0, calc("[1..10].limit(5).sum()"))
        assertEquals(5.0, calc("[1..10].limit(5).last()"))
        assertEquals(5.0, calc("[1..10].limit(5).count()"))
        assertEquals(20.0, calc("[1..10].skip(1).limit(5).sum()"))
    }

    @Test
    fun classes() {
        val init = "@point = class(@x, @y); " +
                "@point.@sum.method{this.@x + this.@y}; " +
                "@point.@sum2.method(@t, {this.@x + this.@y + @t}); " +
                "@point.@sum3.methodref(function(@t, {this.@x + this.@y + @t}));" +
                "@p1 = @point.new(10, 20); " +
                "@p2 = @point.new(10, 20); " +
                "@p3 = @point.new(30, 40); " +

                "@line = class(@begin, @end); " +
                "@l1 = @line.new(@p1, @p3);" +

                "@node = class(@data, @left, @right);" +
                "@n1 = @node.new(1);" +
                "@n2 = @node.new(2);" +
                "@n3 = @node.new(3);" +
                "@n4 = @node.new(4);" +
                "@n1.@left = @n2;" +
                "@n1.@right = @n3;" +
                "@n2.@left = @n4;" +
                "@empty_class = class;" +
                "@empty_class.@m.method(200); " +
                "@empty_obj = @empty_class.new()"


        assertEquals(10.0 , calc("$init; @p1.@x"))
        assertEquals(20.0 , calc("$init; @p1.@y"))
        assertEquals(10.0 , calc("$init; @p2.@x"))
        assertEquals(20.0 , calc("$init; @p2.@y"))
        assertEquals(50.0 , calc("$init; @p1.@x = 50; @p1.@x"))
        assertEquals(20.0 , calc("$init; @p1.@x = 50; @p1.@y"))
        assertEquals(10.0 , calc("$init; @p1.@x = 50; @p2.@x"))
        assertEquals(20.0 , calc("$init; @p1.@x = 50; @p2.@y"))

        assertEquals(30.0 , calc("$init; @p1.@sum.call()"))
        assertEquals(130.0 , calc("$init; @p1.@sum2.call(100)"))
        assertEquals(130.0 , calc("$init; @p1.@sum3.call(100)"))
        assertEquals(170.0 , calc("$init; @p1.@x = 50; @p1.@sum2.call(100)"))

        assertEquals(10.0 , calc("$init; @l1.@begin.@x"))
        assertEquals(40.0 , calc("$init; @l1.@end.@y"))
        assertEquals(1.0 , calc("$init; @n1.@data"))

        assertEquals(2.0 , calc("$init; @n1.@left.@data"))
        assertEquals(3.0 , calc("$init; @n1.@right.@data"))
        assertEquals(4.0 , calc("$init; @n1.@left.@left.@data"))

        assertEquals(200.0 , calc("$init; @empty_obj.@m.call()"))
    }

    @Test
    fun extends() {
        val init = "@point = class(@x, @y); @point.@sum.method(this.@x + this.@y).@mult.method(this.@x * this.@y);" +
                "@point_3d = @point.extends(@z);" +
                "@p1 = @point.new(10, 20); " +
                "@p2 = @point_3d.new(10, 20, 30)"


        assertEquals(30.0 , calc("$init; @p1.@sum.call()"))
        assertEquals(30.0 , calc("$init; @p2.@sum.call()"))
        assertEquals(200.0 , calc("$init; @p2.@mult.call()"))
        assertEquals(30.0 , calc("$init; @p2.@z"))
    }

    @Test
    fun equalsObjects() {
        val init = "@point = class(@x, @y); " +
                "@p1 = @point.new(10, 20); " +
                "@p2 = @point.new(10, 20); " +
                "@p3 = @point.new(10, 40); " +

                "@point2 = class(@x, @y); " +
                "@p4 = @point2.new(10, 20)"


        assertEquals(1.0 , calc("$init; @p1 == @p1"))
        assertEquals(1.0 , calc("$init; @p1 != @p2"))
        assertEquals(1.0 , calc("$init; @p1.equals(@p1)"))
        assertEquals(1.0 , calc("$init; @p1.equals(@p2)"))
        assertEquals(0.0 , calc("$init; @p1.equals(@p3)"))
        assertEquals(1.0 , calc("$init; @p1.equals(@p4)"))
    }

    @Test
    fun sortObjects() {
        val init = "@point = class(@x, @y); " +
                "@p1 = @point.new(6, 'one'); " +
                "@p2 = @point.new(9, 'second'); " +
                "@p3 = @point.new(3, 'third')"


        assertEquals(listOf("third", "one", "second") , calcStringList("$init; " +
                "seq(@p1, @p2, @p3).sortBy(it.@x).map(it.@y)"))
        assertEquals(listOf("second", "one", "third") , calcStringList("$init; " +
                "seq(@p1, @p2, @p3).sortByDesc(it.@x).map(it.@y)"))
    }

    @Test
    fun groupObjects() {
        val init = "@point = class(@x, @y); " +
                "@p1 = @point.new(6, 'one'); " +
                "@p2 = @point.new(9, 'second'); " +
                "@p3 = @point.new(3, 'second')"


        assertEquals(listOf("one", "second") , calcStringList("$init; " +
                "seq(@p1, @p2, @p3).groupBy(it.@y).map(it.groupKey)"))
        assertEquals(listOf(6.0, 9.0, 3.0) , calcList("$init; " +
                "seq(@p1, @p2, @p3).groupBy(it.@y).flatmap(it.values()).map(it.@x)"))
    }

    @Test
    fun equalsCollections() {
        assertEquals(1.0 , calc("list(1,2,3,4).equals(list(1,2,3,4))"))
        assertEquals(0.0 , calc("list(1,2,3,4).equals(list(1,2,3,4,6))"))
        assertEquals(0.0 , calc("list(1,2,3,4).equals(list(1,2,3,5))"))
        assertEquals(1.0 , calc("list(1,2,3,4) != list(1,2,3,4)"))
        assertEquals(1.0 , calc("equals(list(1),list(1),list(1),list(1))"))
        assertEquals(0.0 , calc("equals(list(1),list(1),list(1),list(2))"))
        assertEquals(1.0 , calc("list().equals(list())"))
        assertEquals(1.0 , calc("array(10,1).equals(array(10,1))"))
        assertEquals(1.0 , calc("" +
                "@m1 = hashmap(); @m1.set(1, 2); " +
                "@m2 = hashmap(); @m2.set(1,2); " +
                "@m1.equals(@m2)"))
    }

    @Test
    fun custom() {
        val parser = parserWithFunction("test", 1, 1) {
            context -> (context.operands().firstOperand() ?: 0.0) * 2
        }
        assertEquals(3.0, calc("test(1)+1", parser))
    }

    @Test
    fun eratosthenes() {

        val code = "@n = args.get(0); @m = args.get(1); " +
                   "if (@m <= 1, return 2);" +
                   "@arr = array(floor(@n/2)); @number = 2;" +
                   "range(3, @n, 2).foreach(@current -> {" +
                   "    if (@arr.get[floor(@current / 2) - 1] == 0, {" +
                   "      if (@number++ == @m, return @current)" +
                   "    });" +
                   "    range(@current * @current, @n, @current).foreach(@next -> {" +
                   "       if (@next % 2 == 1, @arr.set[floor(@next / 2) - 1, 1])" +
                   "    })" +
                   "});" +
                   "return 0"

        assertEquals(11.0, calc(code, 1000.0, 5.0))
    }

    @Test
    fun eratosthene2() {

        val code =
            "@erato = class(@max).ctor {" +
                    "this.@current = 2; " +
                    "this.@arr = array(floor(this.@max/2))" +
            "}; " +
            "@erato.@next.method { " +
                "if (this.@current == 2, { this.@current = 3; return 2 });" +
                "range(this.@current ^ 2, this.@max, this.@current).foreach(@i -> {" +
                    "if (@i % 2 == 1, this.@arr.set[floor(@i / 2) - 1, 1])" +
                "});" +
                "@prime = if (this.@arr.get[floor(this.@current / 2) - 1] == 0, this.@current, null);" +
                "this.@current += 2; " +
                "return @prime" +
            "};" +
            "@erato.@primes.method {" +
                "while.map(this.@current < this.@max, ::(this.@next)).filter(it != null)" + //todo brackets
            "};" +
            "@erato.new(args.get(0)).@primes.call().skip(args.get(1)).take(args.get(2))"

        assertEquals(listOf(5.0,7.0,11.0,13.0,17.0), calcList(code, 1000.0, 2.0, 5.0))
    }

    @Test
    fun strings() {
        assertEquals("test" , calcString("'test'"))
        assertEquals("test" , calcString("string.args.get(0)", "test"))
        assertEquals("test" , calcString("@s = 'test'"))
        assertEquals(4.0 , calc("@s = 'test'; @s.length"))
        assertEquals("hello, world!" , calcString("'hello, ' .+ 'world!'"))
        assertEquals("es" , calcString("@s = 'test'; @s.slice(1,3)"))
        assertEquals("es" , calcString("@s = 'test'; @s.substring(1,3)"))
        assertEquals(2.0 , calc("'test'.indexOf('st')"))
        assertEquals(-1.0 , calc("'test'.indexOf('sto')"))
        assertEquals("toast" , calcString("@s = 'test'; @t = @s.replace('es','oas'); @t"))
        assertEquals("test" , calcString("@s = 'test'; @t = @s.replace('es','oas'); @s"))
        assertEquals("TEST" , calcString("'test'.uppercase"))
        assertEquals("resr" , calcString("'TEST'.replace('T','R').lowercase()"))

        assertEquals(true , calcBoolean("'test' == 'test'"))
        assertEquals(true , calcBoolean("@s1 ='test'; @s2 = 'test'; @s1 == @s2"))
        assertEquals(true , calcBoolean("@s1 ='test'; @s2 = 'test'; @s1.equals(@s2)"))
        assertEquals(true , calcBoolean("'a'.+'b' == 'a'.+'b'"))

        assertEquals(listOf("t","e","s","t"), calcStringList("list('t','e','s','t').values"))
        assertEquals(listOf("t","e","s","t"), calcStringList("'test'.split()"))
        assertEquals(listOf("t","st"), calcStringList("'test'.split('e')"))
        assertEquals(listOf("t","st-test-test"), calcStringList("'test-test-test'.split('e', 2)"))
        assertEquals(listOf("t","st-t", "st-test"), calcStringList("'test-test-test'.split('e', 3)"))
        assertEquals(listOf("test-test-test"), calcStringList("'test-test-test'.split('E', 0, 0)"))
        assertEquals(listOf("t","st-t", "st-t", "st"), calcStringList("'test-test-test'.split('E', 0, 1)"))
        assertEquals(listOf("a","b","c","d","e"), calcStringList("'a,b,c,d,,e'.split(',', 0, 0, 1)"))
        assertEquals(listOf("a","b","c","d","","e"), calcStringList("'a,b,c,d,,e'.split(',', 0, 0, 0)"))
        assertEquals("a,b,c,d" , calcString("string.join(',','a','b','c','d')"))

        assertEquals("1.2" , calcString("1.2.toString()"))
        assertEquals("1" , calcString("1.toString('%d')"))
        assertEquals("this is 10 !" , calcString("string.format('this is %d %s', 10, '!')"))

        assertEquals(1.0 , calc("'1'.toNumber()"))
        assertEquals(1.2 , calc("'1.2'.toNumber()"))
        assertEquals(1.2 , calc("1.2.toString().toNumber()"))
        assertEquals(null , calc("'test null'.toNumber()"))
    }

    @Test
    fun loops() {
        assertEquals(listOf(4.0,3.0,2.0,1.0,0.0), calcList("@x = 20; while.map(@x-- > 0, @x).skip(15)"))
        assertEquals(listOf(2.0,3.0,1.0,2.0,0.0,1.0), calcList("@x = 20; while.flatmap(@x-- > 0, @x..@x+1).skip(34)"))

        assertEquals(12.0, calc("@x = 0; while(@x < 12, @x++); @x"))
        assertEquals(12.0, calc("@x = 0; while(true, if (@x++ > 10, break)); @x"))
        assertEquals(21.0, calc("@x = 20; dowhile(true, if (@x++ > 10, break)); @x"))

        assertEquals(listOf(5.0,6.0,7.0,8.0,9.0,10.0), calcList("[1..10].map(if(it >= 5, it, continue))"))
        assertEquals(listOf(1.0,2.0,6.0), calcList("[1..3].map(if(it <= 2, continue.yield(it)); it * 2)"))
        assertEquals(listOf(1.0,2.0), calcList("[1..10].map(if(it > 2, break); it)"))
        assertEquals(3.0, calc("[1..10].foreach(if(it > 2, break.yield(it)))"))
    }

    @Test
    fun io() {
        assertEquals("First!\nSecond!\nThird!\n",
            calcIo("out.writeLines(in.readLines().map(it.+'!'))",
            "First\nSecond\nThird"))

        assertEquals("First!\nSecond!\nThird!\n",
            calcIo("in.readLines().map(it.+'!').foreach(out.writeLine(it))",
                "First\nSecond\nThird"))

        assertEquals("First!\nSecond!\nThird!\n",
            calcIo("out.writeAll(in.readLines().map(it.+'!'.+string.newline))",
                "First\nSecond\nThird"))

        assertEquals("First!Second!Third!",
            calcIo("out.writeAll(in.readLines().map(it.+'!'))",
                "First\nSecond\nThird"))

        assertEquals("First?!Second?!Third?!",
            calcIo("in.readLines().map(it.+'!').foreach(out.write(it))",
                "First?\nSecond?\nThird?"))
    }

    @Suppress("SameParameterValue", "SameParameterValue")
    private fun parserWithFunction(name: String, minOperandCount: Int, maxOperandCount: Int?,
                                   func: (CalcFunctionContext) -> Double) : CalcParser {
        val functionSet = CalcFunctionSet.default()
        functionSet.addFunction(name, func,
                minOperandCount = minOperandCount, maxOperandCount = maxOperandCount)
        return CalcParser(functionSet)
    }

    private fun calc(text: String, vararg args: Double) : Double? {
        return calc(text, _parser, args)
    }

    private fun calc(text: String, parser: CalcParser, args: DoubleArray? = null) : Double? {
        val result = parser.parse(text)
        if (result.error != null) {
            return null
        }
        val input = CalcInput(args = args?.toList() ?: emptyList())
        return result.expression?.evaluate(input)
    }

    private fun calcList(text: String, vararg args: Double) : List<Double>? {
        val result = _parser.parse(text)
        if (result.error != null) {
            return null
        }
        val input = CalcInput(args = args.toList())
        return result.expression?.evaluateSequence(input)?.toList()
    }

    private fun calcString(text: String, vararg args: String) : String? {
        val result = _parser.parse(text)
        if (result.error != null) {
            return null
        }
        val input = CalcInput(stringArgs = args.toList())
        return result.expression?.evaluateString(input)
    }

    private fun calcStringList(text: String) : List<String>? {
        val result = _parser.parse(text)
        if (result.error != null) {
            return null
        }
        val input = CalcInput()
        return result.expression?.evaluateStringSequence(input)?.toList()
    }

    private fun calcIo(text: String, inputString: String) : String? {
        val result = _parser.parse(text)
        if (result.error != null) {
            return null
        }
        val inputStream = ByteArrayInputStream(inputString.toByteArray())
        val outputStream = ByteArrayOutputStream()
        val input = CalcInput(inputStream = inputStream, outputStream = outputStream)
        result.expression?.evaluate(input)
        return outputStream.toString().replace("\r\n","\n")
    }

    private fun calcBoolean(text: String) : Boolean? {
        val result = _parser.parse(text)
        if (result.error != null) {
            return null
        }
        val input = CalcInput()
        return result.expression?.evaluateBoolean(input)
    }
}