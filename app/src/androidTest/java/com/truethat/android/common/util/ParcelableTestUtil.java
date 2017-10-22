package com.truethat.android.common.util;

import android.os.Parcel;
import android.os.Parcelable;

import static org.junit.Assert.assertEquals;

/**
 * Proudly created by ohad on 16/10/2017 for TrueThat.
 */

public class ParcelableTestUtil {
  private static Parcel serialize(Parcelable parcelable) throws Exception {
    Parcel parcel = Parcel.obtain();
    parcelable.writeToParcel(parcel, parcelable.describeContents());
    parcel.setDataPosition(0);
    return parcel;
  }

  private static Object deserialize(Parcel parcel, Parcelable.Creator creator) throws Exception {
    return creator.createFromParcel(parcel);
  }

  public static void testParcelability(Parcelable parcelable, Parcelable.Creator creator)
      throws Exception {
    assertEquals(parcelable, deserialize(serialize(parcelable), creator));
  }
}
