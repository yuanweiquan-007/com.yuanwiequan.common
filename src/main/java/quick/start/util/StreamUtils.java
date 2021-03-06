package quick.start.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author yuanweiquan
 */
public class StreamUtils {

     public static <T> Stream<T> of(Iterator<T> iterator, boolean parallel) {
          return of(() -> iterator, parallel);
     }

     public static <T> Stream<T> of(Iterator<T> iterator) {
          return of(iterator, false);
     }

     public static <T> Stream<T> of(Iterable<T> iterable, boolean parallel) {
          return StreamSupport.stream(iterable.spliterator(), parallel);
     }

     public static <T> Stream<T> of(Iterable<T> iterable) {
          return of(iterable, false);
     }

     public static <T> Stream<T> of(T... ts) {
          return of(Arrays.asList(ts));
     }


}
