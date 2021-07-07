package tech.skargen.skartools;

// import java.util.concurrent.TimeUnit;
// import org.openjdk.jmh.annotations.Benchmark;
// import org.openjdk.jmh.annotations.BenchmarkMode;
// import org.openjdk.jmh.annotations.Fork;
// import org.openjdk.jmh.annotations.Level;
// import org.openjdk.jmh.annotations.Measurement;
// import org.openjdk.jmh.annotations.Mode;
// import org.openjdk.jmh.annotations.OutputTimeUnit;
// import org.openjdk.jmh.annotations.Param;
// import org.openjdk.jmh.annotations.Scope;
// import org.openjdk.jmh.annotations.Setup;
// import org.openjdk.jmh.annotations.State;
// import org.openjdk.jmh.annotations.Warmup;

// @State(Scope.Benchmark)
public class BenchmarkTests {
  // @Fork(value = 1, warmups = 0)
  // @Warmup(iterations = 0, time = 10)
  // @Measurement(iterations = 3, time = 10)
  // @BenchmarkMode(Mode.AverageTime)
  // @OutputTimeUnit(TimeUnit.MILLISECONDS)
  // @State(Scope.Benchmark)
  // public static class TestStrings {
  //   public String text;
  //   @Param({"10000", "100000"}) public int iterations;

  //   @Setup(Level.Invocation)
  //   public void setup() {
  //     String t = "t";
  //     text = "t";
  //     for (int i = 0; i < this.iterations; i++) {
  //       this.text = String.join("", this.text, t);
  //     }
  //   }

  //   // @Benchmark
  //   // public String stringplus() {
  //   //   String t = "";
  //   //   for (int i = 0; i < this.iterations; i++) {
  //   //     t += text;
  //   //   }
  //   //   return t;
  //   // }

  //   // @Benchmark
  //   // public String stringjoin() {
  //   //   String t = "";
  //   //   for (int i = 0; i < this.iterations; i++) {
  //   //     t = String.join("", text);
  //   //   }
  //   //   return t;
  //   // }

  //   @Benchmark
  //   public StringBuilder stringsubsequence() {
  //     StringBuilder t = new StringBuilder(this.iterations);
  //     t.setLength(this.iterations);
  //     t.append(this.text.subSequence(0, this.text.length() - 1));
  //     // for (int i = 0; i < this.iterations; i++) {
  //     // }
  //     return t;
  //   }

  //   @Benchmark
  //   public StringBuilder stringsubstring() {
  //     StringBuilder t = new StringBuilder(this.iterations);
  //     t.setLength(this.iterations);
  //     t.append(this.text.substring(0, this.text.length() - 1));
  //     // for (int i = 0; i < this.iterations; i++) {
  //     // }
  //     return t;
  //   }

  //   @Benchmark
  //   public StringBuilder stringbuildersubsequence() {
  //     StringBuilder n = new StringBuilder(this.text);
  //     StringBuilder t = new StringBuilder(this.iterations);
  //     t.setLength(this.iterations);
  //     t.append(n.subSequence(0, n.length() - 1));
  //     // for (int i = 0; i < this.iterations; i++) {
  //     // }
  //     return t;
  //   }

  //   @Benchmark
  //   public StringBuilder stringbuildersubstring() {
  //     StringBuilder n = new StringBuilder(this.text);
  //     StringBuilder t = new StringBuilder(this.iterations);
  //     t.setLength(this.iterations);
  //     t.append(n.substring(0, n.length() - 1));
  //     // for (int i = 0; i < this.iterations; i++) {
  //     // }
  //     return t;
  //   }

  //   // @Benchmark
  //   // public StringBuilder stringbuilderinsert() {
  //   //   StringBuilder t = new StringBuilder();
  //   //   for (int i = 0; i < this.iterations; i++) {
  //   //     t.insert(0, text);
  //   //   }
  //   //   return t;
  //   // }

  //   // @Benchmark
  //   public StringBuilder stringbuildersetchar() {
  //     StringBuilder n = new StringBuilder(this.text);
  //     StringBuilder t = new StringBuilder();
  //     t.setLength(this.iterations);
  //     for (int i = 0; i < this.iterations; i++) {
  //       t.setCharAt(i, n.charAt(i));
  //     }
  //     return t;
  //   }
  // }

  // @Fork(value = 1, warmups = 0)
  // @Warmup(iterations = 1, time = 10)
  // @Measurement(iterations = 2, time = 10)
  // @BenchmarkMode(Mode.AverageTime)
  // @OutputTimeUnit(TimeUnit.MILLISECONDS)
  // @State(Scope.Benchmark)
  public static class TestTables {
    // @Param({"L", "_", ">"}) public char alignment;
    // @Param({"-"}) public char filler;
    // public int space;
    // public String text;
    // public StringBuilder textasbuilder;
    // @Param({"100000"}) public int iteration;

    // @Setup(Level.Invocation)
    // public void setup() {
    //   this.textasbuilder = new StringBuilder(this.iteration);
    //   for (int i = 0; i < this.iteration; i++) {
    //     this.textasbuilder.append('s');
    //   }
    //   this.text = this.textasbuilder.toString();
    //   this.space = this.iteration + 10;
    // }

    // @Benchmark
    // public StringBuilder voidformat() {
    //   new SkarTables().fillerFormat(alignment, filler, space, textasbuilder);
    //   return textasbuilder;
    // }

    // // @Benchmark
    // public StringBuilder voidformatnew() {
    //   // new SkarTables().fillerFormatnew(alignment, filler, space, textasbuilder);
    //   return textasbuilder;
    // }

    // @Benchmark
    // public StringBuilder returnformat() {
    //   return new SkarTables().fillerFormat(alignment, filler, space, text);
    // }

    // // @Benchmark
    // public String stringformat() {
    //   return new SkarTables().stringFormat(alignment, space, text);
    // }
  }

  // @Fork(value = 1, warmups = 0)
  // @Warmup(iterations = 1, time = 10)
  // @Measurement(iterations = 2, time = 10)
  // @BenchmarkMode(Mode.AverageTime)
  // @OutputTimeUnit(TimeUnit.MILLISECONDS)
  // @State(Scope.Benchmark)
  public static class TestSText {
    // @Param({"L", "_", ">"}) public char alignment;
    // @Param({"-"}) public char filler;
    // public int space;
    // @Param({"-1", "0", "1"}) public int align;
    // @Param({"10000"}) public int iteration;
    // @Param({"s\0\0\0g\0l\0lfsa\0llda;ldl\0\0\0dasdlflasdl\0\0dasd"}) String text;
    // String testString;
    // public StringBuilder testBuilder;
    // public SText stext;

    // @Setup(Level.Invocation)
    // public void setup() {
    //   this.stext = SText.getInstance();
    //   this.testString = "";
    //   this.testBuilder = new StringBuilder(this.iteration);
    //   for (int i = 0; i < this.iteration; i++) {
    //     this.testBuilder.append(text);
    //   }
    //   this.testString = this.testBuilder.toString();
    // }

    // @Benchmark
    // public StringBuilder format() {
    //   return stext.format(testString, testString.length() + 10, align, '-');
    // }

    // @Benchmark
    // public StringBuilder objectformat() {
    //   return stext.format(align, testString.length() + 10, '-', testString);
    // }

    // public void testAll() {
    //   this.stext = SText.getInstance();
    //   this.text = "this is a line";
    //   this.iteration = 1000;
    //   this.testString = "";
    //   this.testBuilder = new StringBuilder(this.iteration);
    //   for (int i = 0; i < this.iteration; i++) {
    //     this.testBuilder.append(text);
    //   }
    //   this.testString = this.testBuilder.toString();

    //   int space = 20;
    //   char fill = '-';

    //   this.text = "s\0\0\0g\0l\0lfsa\0llda;ldl\0\0\0dasdlflasdl\0\0dasd";
    //   System.out.println(text.length());
    //   System.out.println(stext.stripOfChar(text, Character.MIN_VALUE).length());
    // System.out.println(stext.format(text, space, -1, fill));
    // System.out.println(stext.format(text, space, 0, fill));
    // System.out.println(stext.format(text, space, 1, fill));

    // System.out.println(stext.format(-1, space, fill, text));
    // System.out.println(stext.format(0, space, fill, text));
    // System.out.println(stext.format(1, space, fill, text));

    // char repeatable = '-';
    // char corner = '+';
    // System.out.println(stext.sequenceWrap(text, -1, repeatable, corner));
    // System.out.println(stext.sequenceWrap(text, 0, repeatable, corner));
    // System.out.println(stext.sequenceWrap(text, 1, repeatable, corner));
    // }
  }
}
