package org.xbmc.android.account.authenticator.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Button;
import butterknife.ButterKnife;
import butterknife.InjectView;
import co.juliansuarez.libwizardpager.wizard.ui.StepPagerStrip;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import org.xbmc.android.remotesandbox.R;

import static org.xbmc.android.account.authenticator.ui.AbstractWizardFragment.STATUS_DISABLED;
import static org.xbmc.android.account.authenticator.ui.AbstractWizardFragment.STATUS_ENABLED;
import static org.xbmc.android.account.authenticator.ui.AbstractWizardFragment.STATUS_GONE;

public class WizardActivity extends SherlockFragmentActivity {

	@InjectView(R.id.strip) StepPagerStrip pagerStrip;
	@InjectView(R.id.pager) ViewPager pager;

	@InjectView(R.id.next_button) Button nextButton;
	@InjectView(R.id.prev_button) Button prevButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_accountwizard);
		setTitle(R.string.accountwizard_title);
		ButterKnife.inject(this);

		final WizardPagerAdapter adapter = new WizardPagerAdapter(getSupportFragmentManager());
		pagerStrip.setOnPageSelectedListener(new StepPagerStrip.OnPageSelectedListener() {
			@Override
			public void onPageStripSelected(int position) {
				position = Math.min(adapter.getCount() - 1, position);
				if (pager.getCurrentItem() != position) {
					pager.setCurrentItem(position);
				}
			}
		});
		pagerStrip.setPageCount(4);
		pagerStrip.setCurrentPage(0);

		pager.setAdapter(adapter);
		pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				adapter.setCurrentPage(position);
				pagerStrip.setCurrentPage(position);
			}
		});
		updateBottomBar(adapter.getCurrentFragment());
	}

	private void updateBottomBar(AbstractWizardFragment fragment) {
		updateButton(nextButton, fragment.hasNext());
		updateButton(prevButton, fragment.hasPrev());
	}

	private static void updateButton(Button button, int state) {
		switch (state) {
			case STATUS_DISABLED:
				button.setVisibility(View.VISIBLE);
				button.setEnabled(false);
				break;
			case STATUS_ENABLED:
				button.setVisibility(View.VISIBLE);
				button.setEnabled(true);
				break;
			case STATUS_GONE:
				button.setVisibility(View.INVISIBLE);
				break;
		}
	}

	private class WizardPagerAdapter extends FragmentStatePagerAdapter {

		private final static int TOTAL_COUNT = 4;

		private int pagerCount = TOTAL_COUNT;
		private int currentPos = 0;
		private AbstractWizardFragment currentFragment = new Step1WelcomeFragment();

		public WizardPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int i) {
			if (currentPos == i) {
				return currentFragment;
			}
			if (currentPos == i - 1 && currentFragment.hasNext() == STATUS_ENABLED) {
				return currentFragment.getNext();
			}
			if (currentPos == i + 1 && currentFragment.hasPrev() == STATUS_ENABLED) {
				return currentFragment.getPrev();
			}
			return new Fragment();
		}

		@Override
		public int getCount() {
			return pagerCount;
		}

		public AbstractWizardFragment getCurrentFragment() {
			return currentFragment;
		}

		public void setCurrentPage(int i) {
			final AbstractWizardFragment fragment;
			if (i > currentPos) {
				fragment = currentFragment.getNext();

			} else if (i < currentPos) {
				fragment = currentFragment.getPrev();

			} else {
				fragment = currentFragment;
			}
			currentPos = i;
			currentFragment = fragment;
			if (currentFragment.hasNext() != STATUS_ENABLED) {
				pagerCount = currentPos + 1;
			} else {
				pagerCount = TOTAL_COUNT;
			}
			notifyDataSetChanged();
			updateBottomBar(fragment);
		}
	}

}
