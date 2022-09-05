# TODO

## Check deps

```clj
clojure -Sdeps '{:deps {com.github.liquidz/antq {:mvn/version "RELEASE"}}}' -M -m antq.core
```

## Release checklist

- [ ] Secure cookie
- [ ] Raw fitness
- [ ] Fix chickn
- [ ] Terms, License, Dev blog
- [ ] About page
- [ ] Contact page
- [ ] Known issues (mobile view, emails, fixed C)
- [ ] List limits
- [ ] Update deps
- [ ] Help Texts
- [ ] Evolution details page
- [ ] Player fixes
- [ ] W3C check
- [ ] Zaproxy check
- [ ] Wizard/Presets
- [ ] Optimise fitness
- [ ] Roadmap
- [ ] Humanize dates
- [ ] Sponsors
- [ ] Readme
- [ ] Note split

## Post-release checklist

- [ ] Captcha PR


## The road to alpha

- [ ] Explorer chord construction
- [ ] Repetitions
- [ ] Instrument 
- [ ] Logging config
- [ ] Generate a random chromosome based on mode and progression
- [ ] Show the list of chromosomes per evolution iteration
- [ ] Delete old code, upgrade deps
- [ ] Moar modes and chord progression
- [ ] Upvote/Downvote a track
- [ ] Add missing fields: Evolution(abc progression, evolve after, mode), Chromosome(version)
- [ ] Limits
- [ ] Add background task to evolve tracks
- [ ] Indexes, indexes, indexes
- [ ] Mails and templates
- [ ] Invite a user to collaborate on a private track
- [ ] Password reset
- [ ] SSO
- [ ] Home screen
- [ ] Footer
- [ ] Docker
- [ ] Deploy

## Bugs

- [ ] Evolution can be created without a user
- [ ] Tempo not taken into account
- [ ] Sharps/Flats/Naturals
- [ ] A 7 chord out of range

## Questions

- [ ] Initial evolution not needed
- [ ] Pop sizes
- [ ] Rating policies

