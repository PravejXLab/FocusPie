```This is 48-hour sprint, where i will build very concrete and compressed version of my idea, it will be open source on GitHub, and deployable directly on mobile device.```

### Setup flow --
1. Write the small mind map portion and visualize.
2. Writing the code by clarifying step through comments.
3. Do research through process.
4. Commit to GitHub, do a small test on mobile.

### Lets setup repository.
1. Create a GitHub project ```FocusPie```, and keep repository open source.
2. Make the project roadmap here and at last push to GitHub as first commit.

### Full picture of app --
1. User who launched the app is either student or host, if he is host, he will click on ```Host a table``` button.
2. If he is student, he will click on ```Join a table``` button.
3. They will pass through requested permissions and enable adapter pages.
4. API here will find the devices and connect both once they confirm.
5. Host will start the table, and 1-hour timer will start and will be shared in all user devices.
6. If any user including host disconnects or closes the app, the timer will reset to zero immediately.
7. If finishes successfully, they will be shown with ```Congratulations``` page.
8. No database, no persistent, no fallback, if time available, enough good UI.
9. Let's commit up to here and start the next sprint.

### First 6 hours sprint target --
1. Specify all required permissions in manifest file.
2. Assume all permission are granted and permit manually from setting, also enable BT, Wi-Fi and Location adapters manually.
3. When host will click ```Host a table```, the host device will start broadcasting immediately.
4. When user will click ```Join a table```, the user device will search for the table.

### Second 6 hours sprint target --
1. Improve the status, it is always connected, correct it to real status. Make sure to have payload setup as well.
2. Start the timer button in host screen, clicking will move to study page.
3. Timer is hardcoded to 1 hour this time, startTime will be stored in host viewModel, and shared to others via payload.

# Third 6 hours sprint target -- 
1. Start the timer, it should decrease each second and should be same logically. 
2. if any student leaves or disconnect, timer will reset to 0 hour immediately.
3. Rewrite NearbyConnectionManager, it should be separate for logical and separation clarity.
<br>