package com.truethat.android.application.permissions;

import android.os.Bundle;
import com.truethat.android.view.fragment.BaseFragment;

/**
 * Proudly created by ohad on 20/06/2017 for TrueThat.
 */

public class PermissionFragment extends BaseFragment {
  private static final String ARG_PERMISSION = "permission";

  private Permission mPermission;

  public PermissionFragment() {
    // Required empty public constructor
  }

  /**
   * Use this factory method to create a new instance of
   * this fragment using the provided parameters.
   *
   * @param permission Parameter 1.
   *
   * @return A new instance of fragment RageComicDetailsFragment.
   */
  public static PermissionFragment newInstance(Permission permission) {
    PermissionFragment fragment = new PermissionFragment();
    Bundle args = new Bundle();
    args.putSerializable(ARG_PERMISSION, permission);
    fragment.setArguments(args);
    return fragment;
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mPermission = (Permission) getArguments().getSerializable(ARG_PERMISSION);
  }

  @Override protected int getLayoutResId() {
    return mPermission.getRationaleText();
  }
}
