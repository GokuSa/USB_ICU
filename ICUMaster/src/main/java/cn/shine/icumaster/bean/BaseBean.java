package cn.shine.icumaster.bean;

import java.lang.reflect.Field;

public class BaseBean {

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName() + " [");
		getValue(getClass(), sb);
		sb.delete(sb.length() - 2, sb.length());
		sb.append("]");
		return sb.toString();
	}

	private void getValue(Class clazz, StringBuilder sb) {
		String name = BaseBean.class.getName();
		if (clazz.getName().equals(name)) {
			return;
		}
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			try {
				field.setAccessible(true);
				Object object = field.get(this);
				String value = field.getName() + "=" + object + ", ";
				sb.append(value);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		getValue(clazz.getSuperclass(), sb);
	}

}
