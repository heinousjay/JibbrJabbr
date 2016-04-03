package jj;

import com.google.inject.Binder;

/**
 * implemented by the JJModule and any associated binder helpers
 * to allow the various binding types to be expose as mixin interfaces
 *
 * Created by jasonmiller on 4/3/16.
 */
public interface HasBinder {

	Binder binder();
}
