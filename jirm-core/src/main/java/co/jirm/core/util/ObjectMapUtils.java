package co.jirm.core.util;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;


public class ObjectMapUtils {
	
	@SuppressWarnings("unchecked")
	public static void pushPath(final Map<String, Object> m, List<String> names, Object value) {
		Map<String,Object> current = m;
		int j = 0;
		for (String n : names) {
			j++;
			if (j == names.size()) {
				current.put(n, value);
				break;
			}
			Object o = current.get(n);
			if (o == null) {
				Map<String, Object> sub = Maps.newLinkedHashMap();
				current.put(n, sub);
				current = sub;
			}
			else if (o instanceof Map) {
				current = (Map<String,Object>) o;
			}
			else {
				throw new IllegalArgumentException("Cannot set value to " + names);
			}
		}
	}
	
	public static <T> NestedKeyValue<T> getNestedKeyValue(Map<?,?> m, Object... keys) {
		return _getNestedKeyValue(m, null, null, keys);
	}
	
	@SuppressWarnings("unchecked")
	private static <T> NestedKeyValue<T> _getNestedKeyValue(final Map<?,?> m, final List<?> l, final Class<T> c, Object... keys) {
		Object t = null;
		checkArgument(m == null || l == null, "Either m or l should be null");
		Map<?,?> tm = m;
		List<?> tl = l;
		
		int i = 0;
		for(Object k : keys) {
			if (tm != null && tm.containsKey(k)) {
				t =  tm.get(k);
			}
			else if (tl != null && k instanceof Number) {
				t = tl.get(((Number) k).intValue());
			}
			else if ( tl != null && k instanceof String 
					&& ! ((String) k).isEmpty() 
					&&  Ints.tryParse((String)k) != null) {
				t = tl.get(Integer.parseInt(k.toString()));
			}
			else {
				t = null;
				break;
			}
			
			if (t instanceof Map) {
				tm = (Map<?,?>) t;
				tl = null;
			}
			else if (t instanceof List) {
				tl = (List<?>) t;
				tm = null;
			}
			else if ( t == null ) {
				break;
			}
			i++;
		}
		boolean conflict = t != null && c != null && ! c.isInstance(t);
		T object = conflict ? null : (T) t;
		
		
		return new NestedKeyValue<T>(object, keys, i, conflict);
		
	}
	
	public static class NestedKeyValue<T> {
		public final T object;
		public final int depthStopped;
		public final Object[] keys;
		public final boolean conflict;
		
		private NestedKeyValue(T object, Object[] keys, int depthStopped, boolean conflict) {
			super();
			this.object = object;
			this.keys = keys;
			this.depthStopped = depthStopped;
			this.conflict = conflict;
		}
		
		public boolean isPresent() {
			return depthStopped == keys.length;
		}
	}

}