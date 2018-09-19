import { TestBed } from '@angular/core/testing';

import { HrBuddyClient } from './hr-buddy-client.service';

describe('HrBuddyClient', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: HrBuddyClient = TestBed.get(HrBuddyClient);
    expect(service).toBeTruthy();
  });
});
