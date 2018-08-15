package com.shortesttour.ui.main;

import dagger.Component;

@Component (modules = {MainPresenterModule.class})
public interface MainComponent {
    void inject(MainActivity activity);
}
