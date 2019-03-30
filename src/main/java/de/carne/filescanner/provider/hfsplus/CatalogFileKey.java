/*
 * Copyright (c) 2007-2019 Holger de Carne and contributors, All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.carne.filescanner.provider.hfsplus;

import java.util.Arrays;

import org.eclipse.jdt.annotation.Nullable;

final class CatalogFileKey implements Comparable<CatalogFileKey> {

	private final int parentId;
	private final String name;

	public CatalogFileKey(int parentId, String name) {
		this.parentId = parentId;
		this.name = name;
	}

	public int parentId() {
		return this.parentId;
	}

	public String name() {
		return this.name;
	}

	@Override
	public int compareTo(CatalogFileKey o) {
		int comparison;

		if (this.parentId != o.parentId) {
			comparison = Long.compare(Integer.toUnsignedLong(this.parentId), Integer.toUnsignedLong(o.parentId));
		} else {
			comparison = this.name.compareTo(o.name);
		}
		return comparison;
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(new int[] { this.parentId, this.name.hashCode() });
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		return this == obj || (obj instanceof CatalogFileKey && compareTo((CatalogFileKey) obj) == 0);
	}

	@Override
	public String toString() {
		return "{" + this.parentId + "}/" + this.name;
	}

}
