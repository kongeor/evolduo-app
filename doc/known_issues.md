# Known Issues

## Oh no, my email?!

Evolduo is using Mailjet as its primary email sending provider. It's on a free tier, so the number of emails that can
go out are limited.

As a fallback it is using a self-hosted mail server. This helps reducing operational costs.

The problem is that many email providers will just throw those email to spam (e.g. gmail) or
will straight out reject them (e.g. office365, see [this](https://answers.microsoft.com/en-us/outlook_com/forum/all/hotmailoutlook-block-list-s3140-blocks-all-new/699f3a56-406e-4804-97e2-cbe23b9bb01c?page=1)
for more details).

If you don't get any emails, which you need at least in order to verify your account,
please [contact me](https://evolduo.cons.gr/contact), so I can do it manually.

If your account is verified you will be permitted to create Evolutions, invite friends
and rate tracks.

## Missing account actions

The following account actions are missing:

- Resend verification email
- Change password
- Change email
- Forgot my password

For now if you have trouble with any of those issues please [contact me](https://evolduo.cons.gr/contact).


## Mobile devices

This application is not optimised for small screen and there is no plan in doing in.

## Library Search

... functionality is non-existing.

## Limited key selection

At the moment only a few keys are available for selection. The reason for this is that some
combinations yield invalid results. For example [Eb phrygian](https://evolduo.cons.gr/explorer?key=Eb&mode=phrygian&progression=I-I-I-I&chord=R&tempo=110)
has a mix of flat key notes but the notes have sharps.

This shouldn't happen and this is why this option isn't available when creating an new Evolution.

The problem is related to the way midi notes (60, 62, 70 etc.) are translated to abc notation
which can then be rendered by abc.js Those invalid notes will not sound right and can confuse
the listener.

This is likely not a trivial problem to fix and not something that I will be dealing with in
the foreseeable future mainly because evolduo itself operates at the numeric level of notes
and doesn't fully understand musical semantics. This makes this issue an implementation detail.

As a workaround, if someone is very eager to use Eb phrygian or some other combination that is
not available, using C and then transposing the midi to the desired key is a possible 
postprocessing workaround.
