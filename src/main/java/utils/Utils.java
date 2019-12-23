package utils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class Utils {
    public static <T> T[] concatArrays(T[] arr1, T[] arr2) {
        T[] out = (T[]) Array.newInstance(arr1.getClass().getComponentType(), arr1.length + arr2.length);
        System.arraycopy(arr1, 0, out, 0, arr1.length);
        System.arraycopy(arr2, 0, out, arr1.length, arr2.length);
        return out;
    }

    public static int[] concatArrays(int[] arr1, int[] arr2) {
        int[] out = new int[arr1.length + arr2.length];
        System.arraycopy(arr1, 0, out, 0, arr1.length);
        System.arraycopy(arr2, 0, out, arr1.length, arr2.length);
        return out;
    }

    public static IntConsumer catching(ThrowingIntConsumer ic) {
        return i -> {
            try {
                ic.accept(i);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                throw new RuntimeException(throwable);
            }
        };
    }

    public static IntSupplier catching(ThrowingIntSupplier ic) {
        return () -> {
            try {
                return ic.getAsInt();
            } catch (Throwable throwable) {
                if (!(throwable instanceof ThreadDeath)) {
                    throwable.printStackTrace();
                    throw new RuntimeException(throwable);
                } else {
                    throw (ThreadDeath) throwable;
                }
            }
        };
    }

    public static <T> Supplier<T> catching(ThrowingSupplier<T> ic) {
        return () -> {
            try {
                return ic.get();
            } catch (Throwable throwable) {
                if (!(throwable instanceof ThreadDeath)) {
                    throwable.printStackTrace();
                    throw new RuntimeException(throwable);
                } else {
                    throw (ThreadDeath) throwable;
                }
            }
        };
    }

    public static <OC extends Collection<T>, C extends Collection<? extends Collection<T>>, T> Iterator<OC> permutations(
            C collections, Supplier<OC> collectionFactory) {
        return new Iterator<OC>() {
            List<? extends Collection<T>> collList = new ArrayList<>(collections);
            List<Iterator<T>> iterators = new ArrayList<>();
            List<T> currentList = new ArrayList<>();
            boolean first = true;

            {
                for (Collection<T> ts : collList) {
                    Iterator<T> iterator = ts.iterator();
                    iterators.add(iterator);
                    if (iterator.hasNext()) {
                        currentList.add(iterator.next());
                    } else {
                        currentList.add(null);
                    }
                }
            }

            private boolean hasNext(int i) {
                if (i >= iterators.size()) {
                    return false;
                }
                return iterators.get(i).hasNext() || hasNext(i + 1);
            }

            private void next(int i) {
                if (i >= iterators.size()) {
                    throw new NoSuchElementException();
                }
                if (!iterators.get(i).hasNext()) {
                    iterators.set(i, collList.get(i).iterator());
                    next(i + 1);
                }
                T next = iterators.get(i).next();
                currentList.set(i, next);
            }

            @Override
            public boolean hasNext() {
                return first || hasNext(0);
            }

            @Override
            public OC next() {
                if (!first) {
                    next(0);
                }
                first = false;
                OC coll = collectionFactory.get();
                coll.addAll(currentList);
                return coll;
            }
        };
    }

    public static <T> void forAllPermutations(T[] elements, Consumer<T[]> consumer) {
        forAllPermutations(elements.length, elements, consumer);
    }

    public static <T> void forAllPermutations(int n, T[] elements, Consumer<T[]> consumer) {
        if (n == 1) {
            consumer.accept(elements);
        } else {
            for (int i = 0; i < n - 1; i++) {
                forAllPermutations(n - 1, elements, consumer);
                if (n % 2 == 0) {
                    swap(elements, i, n - 1);
                } else {
                    swap(elements, 0, n - 1);
                }
            }
            forAllPermutations(n - 1, elements, consumer);
        }
    }

    private static <T> void swap(T[] input, int a, int b) {
        T tmp = input[a];
        input[a] = input[b];
        input[b] = tmp;
    }

    public static <T> List<List<T>> permutations(List<T> list) {
        if (list.isEmpty()) {
            List<List<T>> result = new ArrayList<>();
            result.add(new ArrayList<>());
            return result;
        }
        List<T> original = new ArrayList<>(list);
        T firstElement = original.remove(0);
        List<List<T>> returnValue = new ArrayList<>();
        List<List<T>> permutations = permutations(original);
        for (List<T> smallerPermutated : permutations) {
            for (int index = 0; index <= smallerPermutated.size(); index++) {
                List<T> temp = new ArrayList<>(smallerPermutated);
                temp.add(index, firstElement);
                returnValue.add(temp);
            }
        }
        return returnValue;
    }

    public interface ThrowingIntConsumer {
        void accept(int i) throws Throwable;
    }

    public interface ThrowingIntSupplier {
        int getAsInt() throws Throwable;
    }


    public interface ThrowingSupplier<T> {
        T get() throws Throwable;
    }
}
